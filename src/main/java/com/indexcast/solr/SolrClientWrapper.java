package com.indexcast.solr;

import com.indexcast.component.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;


/**
 * This class is used as a wrapper for Solrj client instance.
 *
 * @author Aleksei Ermak
 */

@Slf4j
public class SolrClientWrapper {

    private final String solrHost;
    private final String coreName;
    private final SolrClient client;
    private final int waitMillisecondsIfFail;

    public SolrClientWrapper(String url, SolrClient client, String coreName, int waitIfFail) {
        this.solrHost = url;
        this.client = client;
        this.coreName = coreName;
        this.waitMillisecondsIfFail = waitIfFail;
    }

    public Pair<String, Integer> queryCursorAndDocsToMigrate(SolrQuery query) {
        while (true) {
            try {
                QueryResponse response = client.query(coreName, query);
                return new Pair<>(response.getNextCursorMark(), response.getResults().size());
            } catch (Throwable e) {
                log.error("Can't request source Solr index at " + solrHost + " for a cursor!");
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
            } catch (Throwable e) {
                log.error("Can't request source Solr index at " + solrHost + " for a cursor!");
                e.printStackTrace();
                pingSolrAndWait();
            }
        }
    }

    public boolean index(SolrInputDocument doc) {
        while (true) {
            try {
                client.add(coreName, doc);
                return true;
            } catch (SolrServerException | IOException e) {
                log.error("Can't index document at " + solrHost + "!");
                e.printStackTrace();
                pingSolrAndWait();
            } catch (BaseHttpSolrClient.RemoteSolrException e) {
                log.error("Can't index document at " + solrHost + " due to remote server error!");
                e.printStackTrace();
                return false;
            }
        }
    }

    public void commit() {
        try {
            client.commit(coreName);
        } catch (SolrServerException | IOException e) {
            log.error("Can't commit changes at " + solrHost + "!");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            log.error("Can't close connection with Solr at " + solrHost + "!");
            e.printStackTrace();
        }
    }

    private void pingSolrAndWait() {
        while (true) {
            try {
                SolrPingResponse pingResponse = client.ping(coreName);
                int status = pingResponse.getStatus();
                log.warn("Solr at " + solrHost + " returns ping status " + status +
                        ". Retry operation after " + (waitMillisecondsIfFail/1000) + " second(s)...");
                waitSomeMilliseconds();
                return;
            } catch (NullPointerException | SolrServerException | IOException e) {
                log.warn("Lost connection with " + solrHost +
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
