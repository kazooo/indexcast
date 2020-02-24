package cz.mzk.solr;

import cz.mzk.component.Pair;
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
 * This class is used as a wrapper for Solrj client instance.
 *
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

    public void setupCustomSolrClient(SolrClient client) {
        this.client = client;
    }

    public Pair<String, SolrDocumentList> queryWithCursor(SolrQuery query) {
        final QueryResponse response;
        try {
            response = client.query(coreName, query);
            return new Pair<>(response.getNextCursorMark(), response.getResults());
        } catch (SolrServerException | IOException e) {
            logger.error("Can't request source Solr index for a cursor!");
            e.printStackTrace();
        }
        return null;
    }

    public Pair<String, Integer> queryCursorAndNumFound(SolrQuery query) {
        final QueryResponse response;
        try {
            response = client.query(coreName, query);
            return new Pair<>(response.getNextCursorMark(), response.getResults().size());
        } catch (SolrServerException | IOException e) {
            logger.error("Can't request source Solr index for a cursor!");
            e.printStackTrace();
        }
        return null;
    }

    public boolean index(SolrInputDocument doc) {
        try {
            client.add(coreName, doc);
            return true;
        } catch (SolrServerException | IOException e) {
            logger.error("Can't index document!");
            e.printStackTrace();
            return false;
        }
    }

    public void commit() {
        try {
            client.commit(coreName);
        } catch (SolrServerException | IOException e) {
            logger.error("Can't commit changes!");
            e.printStackTrace();
        }
    }
}
