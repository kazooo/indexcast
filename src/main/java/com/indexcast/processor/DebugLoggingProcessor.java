package com.indexcast.processor;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Simple processor for debug logging.
 *
 * @author Aleksei Ermak
 */

public class DebugLoggingProcessor implements ProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(DebugLoggingProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> inputDocs) {
        logger.debug("[debug-processor] [got] " + inputDocs.size() + " documents.");
        return inputDocs;
    }
}
