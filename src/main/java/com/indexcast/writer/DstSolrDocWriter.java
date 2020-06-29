package com.indexcast.writer;

import com.indexcast.solr.DstSolrClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@AllArgsConstructor
public class DstSolrDocWriter implements ItemWriter<List<SolrInputDocument>> {

    private final DstSolrClient solrClient;

    @Override
    public void write(List<? extends List<SolrInputDocument>> items) {
        for (List<SolrInputDocument> docs : items) {
            for (SolrInputDocument doc : docs) {
                solrClient.index(doc);
            }
            log.debug("[doc-writer][send] " + docs.size() + " docs");
        }
        solrClient.commit();
    }
}
