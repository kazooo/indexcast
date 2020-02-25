package com.indexcast.processor;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Test processor, does nothing and successfully.
 *
 * @author Aleksei Ermak
 */

public class TestProcessor implements ProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(TestProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> item) {
        logger.debug("[test-processor][process] got " + item.size() + " docs");
        return item;
    }
}
