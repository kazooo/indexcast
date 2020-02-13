package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public interface SolrDocProcessor extends ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> {
    public List<SolrInputDocument> process(List<SolrInputDocument> item);
}
