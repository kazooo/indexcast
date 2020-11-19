package com.indexcast.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;

import java.util.Collections;
import java.util.Date;
import java.util.List;


@Slf4j
public class DnntAugmentProcessor implements ProcessorInterface {

    public final String DNNT_FIELD_NAME = "dnnt";
    public final String TIMESTAMP_FIELD_NAME = "timestamp";

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> inputDocs) {
        // suppose all inputDocs contain only the 'PID' field
        // and the Solr schema already has a 'dnnt' field
        // so processor just set the 'dnnt' field to true
        // also update timestamp field
        inputDocs.forEach(d -> {
            d.addField(DNNT_FIELD_NAME, Collections.singletonMap("set", true));
            d.addField(TIMESTAMP_FIELD_NAME, Collections.singletonMap("set", new Date()));
        });
        return inputDocs;
    }
}
