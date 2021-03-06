package com.indexcast.reader;

import com.indexcast.component.CursorMarkGlobalStorage;
import com.indexcast.component.Pair;
import com.indexcast.solr.SrcSolrClient;
import com.indexcast.configuration.IndexcastParameterConfiguration;
import com.indexcast.component.MigrationYAMLSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;


/**
 * This reader requests source Solr instance for batches of documents.
 * If reader starts fetching new part it read cursor mark and docs-to-migrate number representing how many documents
 * can be read from source Solr instance from storage. Then it requests Solr instance for documents
 * in several cycles  until they count reach docs-to-migrate number.
 *
 * @author Aleksei Ermak
 */

@Slf4j
public class SrcSolrDocReader implements ItemReader<List<SolrInputDocument>> {

    private final SrcSolrClient solrClient;
    private final CursorMarkGlobalStorage cursorStorage;

    private final int defaultDocsPerRequest;
    private String lastCursorMark;
    private int maxObjects;
    private int done;

    private final String uniqKey;
    private final String queryStr;
    private final MigrationYAMLSchema schema;

    /**
     * Cursor reader constructor. Set initial cursor value at null to start fetching new part.
     *
     * @param config   Indexcast configuration
     * @param storage  cursor mark storage
     * @param client   source Solr instance client
     */
    public SrcSolrDocReader(IndexcastParameterConfiguration config,
                            CursorMarkGlobalStorage storage,
                            SrcSolrClient client) {
        lastCursorMark = null;
        defaultDocsPerRequest = 1000;
        maxObjects = 0;
        done = 0;
        uniqKey = config.getUniqKey();
        queryStr = config.getQuery();
        schema = config.getMigrationYAMLSchema();

        solrClient = client;
        cursorStorage = storage;
    }

    @Override
    public List<SolrInputDocument> read() {
        if (lastCursorMark == null || done >= maxObjects) {
            Pair<String, Integer> cursorWithMaxObj = cursorStorage.getNextCursorAndObjNum();
            if (cursorWithMaxObj == null) {
                log.debug("[doc-reader][finish] storage contains no cursor marks");
                return null;
            }
            done = 0;
            lastCursorMark = cursorWithMaxObj.getKey();
            maxObjects = cursorWithMaxObj.getValue();
            log.debug("[doc-reader][read] " + lastCursorMark + " " + maxObjects);
        }

        SolrQuery query = generateQueryFromCursor(lastCursorMark);
        Pair<String, SolrDocumentList> nextCursorWithDocs = solrClient.queryWithCursor(query);
        lastCursorMark = nextCursorWithDocs.getKey();
        SolrDocumentList docs = nextCursorWithDocs.getValue();
        done += docs.size();

        return convertToInputDocs(docs);
    }

    private SolrQuery generateQueryFromCursor(String cursor) {
        SolrQuery parameters = new SolrQuery();
        parameters.setQuery(queryStr);
        parameters.setRows(getRows());
        parameters.setRequestHandler("select");
        parameters.setSort(SolrQuery.SortClause.asc(uniqKey));
        parameters.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);
        schema.getRequestFields().forEach(parameters::addField);
        return parameters;
    }

    private List<SolrInputDocument> convertToInputDocs(SolrDocumentList docs) {
        List<SolrInputDocument> inputDocs = new ArrayList<>();
        for (SolrDocument doc : docs) {
            SolrInputDocument inputDoc = schema.convert(doc);
            inputDocs.add(inputDoc);
        }
        return inputDocs;
    }

    private int getRows() {
        return Math.min((maxObjects - done), defaultDocsPerRequest);
    }
}
