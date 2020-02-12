package cz.mzk.writer;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class DstSolrDocWriter implements ItemWriter<List<SolrInputDocument>> {

    @Override
    public void write(List<? extends List<SolrInputDocument>> items) {

    }
}
