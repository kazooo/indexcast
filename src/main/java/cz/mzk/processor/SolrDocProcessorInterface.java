package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public interface SolrDocProcessorInterface extends ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> {
    List<SolrInputDocument> process(List<SolrInputDocument> item);
}
