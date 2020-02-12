package cz.mzk.processor;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class ToInputDocProcessor implements ItemProcessor<SolrDocumentList, List<SolrInputDocument>> {

    @Override
    public List<SolrInputDocument> process(SolrDocumentList item) {
        return null;
    }
}
