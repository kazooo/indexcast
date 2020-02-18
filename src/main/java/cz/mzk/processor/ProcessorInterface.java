package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;


/**
 * Simplified interface for ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> type.
 * Do nothing just make processor type name short and lightweight.
 *
 * @author Aleksei Ermak
 */

public interface ProcessorInterface extends ItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> {
    List<SolrInputDocument> process(List<SolrInputDocument> item);
}
