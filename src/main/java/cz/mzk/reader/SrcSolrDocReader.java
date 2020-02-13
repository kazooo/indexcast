package cz.mzk.reader;

import cz.mzk.configuration.ToolParameterConfiguration;
import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.model.MigrationYAMLSchema;
import cz.mzk.solr.SrcSolrClient;
import javafx.util.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class SrcSolrDocReader implements ItemReader<List<SolrInputDocument>> {

    SrcSolrClient solrClient;
    CursorMarkGlobalStorage cursorStorage;

    private String lastCursorMark;
    private int docsPerRequest;
    private int maxObjects;
    private int done;

    private String uniqKey;
    private String queryStr;
    private MigrationYAMLSchema schema;

    private final Logger logger = LoggerFactory.getLogger(SrcSolrDocReader.class);

    public SrcSolrDocReader(ToolParameterConfiguration config,
                            CursorMarkGlobalStorage storage,
                            SrcSolrClient client) {
        lastCursorMark = null;
        docsPerRequest = 1000;
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
            if (cursorWithMaxObj == null) return null;
            lastCursorMark = cursorWithMaxObj.getKey();
            maxObjects = cursorWithMaxObj.getValue();
            logger.info("[got] " + lastCursorMark);
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
        parameters.setRequestHandler("select");
        parameters.setSort(SolrQuery.SortClause.asc(uniqKey));
        parameters.setRows(docsPerRequest);
        parameters.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);
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
}
