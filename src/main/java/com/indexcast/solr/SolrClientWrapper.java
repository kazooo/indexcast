package com.indexcast.solr;

import com.indexcast.component.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
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
    private String solrHost;
    private String coreName;
    private int waitMillisecondsIfFail;
    private final Logger logger = LoggerFactory.getLogger(SolrClientWrapper.class);

    public SolrClientWrapper(String url, String coreName, int waitIfFail) {
        this.coreName = coreName;
        this.solrHost = url;
        this.waitMillisecondsIfFail = waitIfFail;
        client = new HttpSolrClient.Builder(url)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public void setupCustomSolrClient(SolrClient client) {
        this.client = client;
    }

    public Pair<String, Integer> queryCursorAndDocsToMigrate(SolrQuery query) {
        while (true) {
            try {
                QueryResponse response = client.query(coreName, query);
                return new Pair<>(response.getNextCursorMark(), response.getResults().size());
            } catch (SolrServerException | IOException e) {
                logger.error("Can't request source Solr index at " + solrHost + " for a cursor!");
                e.printStackTrace();
                pingSolrAndWait();
            }
        }
    }

    public Pair<String, SolrDocumentList> queryWithCursor(SolrQuery query) {
        while (true) {
            try {
                QueryResponse response = client.query(coreName, query);
                return new Pair<>(response.getNextCursorMark(), response.getResults());
            } catch (SolrServerException | IOException e) {
                logger.error("Can't request source Solr index at " + solrHost + " for a cursor!");
                e.printStackTrace();
                pingSolrAndWait();
            }
        }
    }

    public void index(SolrInputDocument doc) {
        while (true) {
            try {
                client.add(coreName, doc);
                return;
            } catch (SolrServerException | IOException e) {
                logger.error("Can't index document at " + solrHost + "!");
                e.printStackTrace();
                pingSolrAndWait();
            }
        }
    }

    public void commit() {
        try {
            client.commit(coreName);
        } catch (SolrServerException | IOException e) {
            logger.error("Can't commit changes at " + solrHost + "!");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Can't close connection with Solr at " + solrHost + "!");
            e.printStackTrace();
        }
    }

    private void pingSolrAndWait() {
        while (true) {
            try {
                SolrPingResponse pingResponse = client.ping(coreName);
                int status = pingResponse.getStatus();
                logger.warn("Solr at " + solrHost + " returns ping status " + status +
                        ". Retry operation after " + (waitMillisecondsIfFail/1000) + " second(s)...");
                waitSomeMilliseconds();
                return;
            } catch (NullPointerException | SolrServerException | IOException e) {
                logger.warn("Lost connection with " + solrHost +
                        "! Check ping status again after " + (waitMillisecondsIfFail/1000) + " second(s)...");
                waitSomeMilliseconds();
            }
        }
    }

    private void waitSomeMilliseconds() {
        try {
            Thread.sleep(waitMillisecondsIfFail);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
