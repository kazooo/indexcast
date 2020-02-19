package cz.mzk.reader;

import cz.mzk.configuration.IndexcastParameterConfiguration;
import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.component.Pair;
import cz.mzk.solr.SrcSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;


/**
 * This reader fetch cursors from source Solr instance.
 * With cursor it requests for numFound number representing how many objects can read from previous cursor.
 *
 * @author Aleksei Ermak
 */

public class SrcSolrCursorReader implements ItemReader<Pair<String, Integer>> {

    IndexcastParameterConfiguration toolConfiguration;
    CursorMarkGlobalStorage cursorStorage;
    SrcSolrClient solrClient;

    private String lastCursorMark;
    private final Logger logger = LoggerFactory.getLogger(SrcSolrCursorReader.class);

    /**
     * Cursor reader constructor. Set initial cursor value at "*" as start cursor mark.
     *
     * @param config   Indexcast configuration
     * @param storage  cursor mark storage
     * @param client   source Solr instance client
     */
    public SrcSolrCursorReader(IndexcastParameterConfiguration config,
                               CursorMarkGlobalStorage storage,
                               SrcSolrClient client) {
        lastCursorMark = CursorMarkParams.CURSOR_MARK_START;
        toolConfiguration = config;
        cursorStorage = storage;
        solrClient = client;
    }

    /**
     * Function requests cursors and numFound numbers from source Solr instance.
     * If the last fetched cursor is the same as previous cursor mark, close cursor storage and return null.
     * Otherwise return previous cursor mark and how many documents can be fetched from this cursor.
     *
     * @return null or pair structure with cursor and numFound number
     */
    @Override
    public Pair<String, Integer> read() {
        SolrQuery query = generateQueryFromCursor(lastCursorMark);
        Pair<String, Long> cursorAndNumFound = solrClient.queryCursorAndNumFound(query);
        String nextCursorMark = cursorAndNumFound.getKey();

        if (nextCursorMark.equals(lastCursorMark)) { // no more cursors, end of index
            cursorStorage.close();
            logger.info("[cursor-reader][finish] reach the end of index, close cursor mark storage");
            return null;
        } else {
            logger.info("[cursor-reader][read] " + lastCursorMark);
            int docsPart = Math.min(toolConfiguration.getDocsPerCycle(), (int) (long) cursorAndNumFound.getValue());
            Pair<String, Integer> pair = new Pair<>(lastCursorMark, docsPart);
            lastCursorMark = nextCursorMark;
            return pair;
        }
    }

    /**
     * Creates query structure for cursor requesting. Uses Indexcast configuration to set up sorting by Solr uniqueKey,
     * query specified by user and how many documents can be processed in one part.
     *
     * @param cursor  cursor from which can be requested source Solr instance for next cursor
     * @return        composed Solr query structure
     */
    private SolrQuery generateQueryFromCursor(String cursor) {
        String uniqKey = toolConfiguration.getUniqKey();
        String queryStr = toolConfiguration.getQuery();
        int maxObjectPerCursor = toolConfiguration.getDocsPerCycle();

        SolrQuery parameters = new SolrQuery();
        parameters.setQuery(queryStr);
        parameters.setRequestHandler("select");
        parameters.setSort(SolrQuery.SortClause.asc(uniqKey));
        parameters.setFields(uniqKey); // at this moment we dont want all data in documents
        parameters.setRows(maxObjectPerCursor);
        parameters.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);
        return parameters;
    }
}
