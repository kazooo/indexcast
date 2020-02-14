package cz.mzk.reader;

import cz.mzk.configuration.IndexcastParameterConfiguration;
import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.model.Pair;
import cz.mzk.solr.SrcSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;


/**
 * @author Aleksei Ermak
 */

public class SrcSolrCursorReader implements ItemReader<Pair<String, Integer>> {

    IndexcastParameterConfiguration toolConfiguration;
    CursorMarkGlobalStorage cursorStorage;
    SrcSolrClient solrClient;

    private String lastCursorMark;
    private final Logger logger = LoggerFactory.getLogger(SrcSolrCursorReader.class);

    public SrcSolrCursorReader(IndexcastParameterConfiguration config,
                               CursorMarkGlobalStorage storage,
                               SrcSolrClient client) {
        lastCursorMark = CursorMarkParams.CURSOR_MARK_START;
        toolConfiguration = config;
        cursorStorage = storage;
        solrClient = client;
    }

    @Override
    public Pair<String, Integer> read() {
        SolrQuery query = generateQueryFromCursor(lastCursorMark);
        String nextCursorMark = solrClient.queryCursor(query);

        if (nextCursorMark.equals(lastCursorMark)) { // no more cursors, end of index
            cursorStorage.close();
            return null;
        } else {
            logger.debug("[read] " + lastCursorMark);
            Pair<String, Integer> pair = new Pair<>(lastCursorMark, query.getRows());
            lastCursorMark = nextCursorMark;
            return pair;
        }
    }

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
