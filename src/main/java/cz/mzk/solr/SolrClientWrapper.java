package cz.mzk.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;


/**
 * @author Aleksei Ermak
 */

public class SolrClientWrapper {

    private SolrClient client;
    private String coreName;

    public SolrClientWrapper(String url, String coreName) {
        this.coreName = coreName;
        client = new HttpSolrClient.Builder(url)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public SolrDocumentList query(SolrQuery query) {
        final QueryResponse response;
        try {
            response = client.query(coreName, query);
            return response.getResults();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void index(SolrInputDocument doc) {
        try {
            client.add(doc);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            client.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }
}
