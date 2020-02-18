package cz.mzk.processor;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

public class FakeProcessor implements ProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(TestProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> item) {
        logger.debug("I'm fake processor!");
        return item;
    }
}
