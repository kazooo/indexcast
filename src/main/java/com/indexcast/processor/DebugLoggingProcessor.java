package com.indexcast.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;

import java.util.List;


/**
 * Simple processor for debug logging.
 *
 * @author Aleksei Ermak
 */
@Slf4j
public class DebugLoggingProcessor implements ProcessorInterface {

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> inputDocs) {
        log.debug("[debug-processor][got] " + inputDocs.size() + " docs.");
        return inputDocs;
    }
}
