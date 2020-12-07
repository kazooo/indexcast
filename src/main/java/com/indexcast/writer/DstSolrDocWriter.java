package com.indexcast.writer;

import com.indexcast.solr.DstSolrClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;
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
        int indexed = 0;
        for (List<SolrInputDocument> docs : items) {
            for (SolrInputDocument doc : docs) {
                if (solrClient.index(doc)) {
                    indexed++;
                }
            }
            log.debug("[doc-writer][indexed] " + indexed + " docs");
        }
        solrClient.commit();
    }
}
