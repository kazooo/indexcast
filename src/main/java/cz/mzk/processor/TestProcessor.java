package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class TestProcessor implements SolrDocProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(TestProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> item) {
        logger.debug("I'm test processor doing nothing and successfully!");
        return item;
    }
}
