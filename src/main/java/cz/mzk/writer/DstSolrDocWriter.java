package cz.mzk.writer;

import cz.mzk.solr.DstSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * This writer sends processed Solr input documents to destination Solr instance.
 * At the end commits changes.
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
                solrClient.index(doc);
            }
            logger.debug("[send] " + docs.size() + " docs");
        }
        solrClient.commit();
    }
}
