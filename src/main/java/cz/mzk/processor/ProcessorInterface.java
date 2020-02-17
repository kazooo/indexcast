package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;


/**
 * Simplified interface for ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> type.
 *
 * @author Aleksei Ermak
 */

public interface ProcessorInterface extends ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> {
    List<SolrInputDocument> process(List<SolrInputDocument> item);
}
