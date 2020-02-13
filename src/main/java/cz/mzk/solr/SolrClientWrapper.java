package cz.mzk.solr;

import javafx.util.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @author Aleksei Ermak
 */

public class SolrClientWrapper {

    private SolrClient client;
    private String coreName;
    private final Logger logger = LoggerFactory.getLogger(SolrClientWrapper.class);

    public SolrClientWrapper(String url, String coreName) {
        this.coreName = coreName;
        client = new HttpSolrClient.Builder(url)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public Pair<String, SolrDocumentList> queryWithCursor(SolrQuery query) {
        final QueryResponse response;
        try {
            response = client.query(coreName, query);
            return new Pair<>(response.getNextCursorMark(), response.getResults());
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String queryCursor(SolrQuery query) {
        final QueryResponse response;
        try {
            response = client.query(coreName, query);
            return response.getNextCursorMark();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void index(SolrInputDocument doc) {
        try {
            client.add(doc);
        } catch (SolrServerException | IOException e) {
            logger.error("Can't index document!");
        }
    }

    public void commit() {
        try {
            client.commit();
        } catch (SolrServerException | IOException e) {
            logger.error("Can't commit changes!");
        }
    }
}
