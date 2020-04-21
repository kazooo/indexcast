package com.indexcast.processor;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class FakeProcessor implements ProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(FakeProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> item) {
        logger.info("[fake-processor][process] got " + item.size() + " docs");
        return item;
    }
}
