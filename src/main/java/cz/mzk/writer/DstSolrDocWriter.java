package cz.mzk.writer;

import cz.mzk.solr.DstSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * This writer sends processed Solr input documents to destination Solr instance.
 * At the end commits changes. If any connection errors occurred, waits for 1 minute and tries again.
 *
 * @author Aleksei Ermak
 */

public class DstSolrDocWriter implements ItemWriter<List<SolrInputDocument>> {

    private DstSolrClient solrClient;
    private final Logger logger = LoggerFactory.getLogger(DstSolrDocWriter.class);

    public DstSolrDocWriter(DstSolrClient dstSolrClient) {
        solrClient = dstSolrClient;
    }

    @Override
    public void write(List<? extends List<SolrInputDocument>> items) {
        for (List<SolrInputDocument> docs : items) {
            for (SolrInputDocument doc : docs) {
                while(!solrClient.index(doc)) {
                    waitForConnection();
                }
            }
            logger.info("[doc-writer][send] " + docs.size() + " docs");
        }
        solrClient.commit();
    }

    private void waitForConnection() {
        try {
            Thread.sleep(60000); // one minute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
