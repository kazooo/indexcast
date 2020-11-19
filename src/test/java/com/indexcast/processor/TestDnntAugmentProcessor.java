package com.indexcast.processor;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class TestDnntAugmentProcessor {

    private DnntAugmentProcessor processor;

    @Before
    public void init() {
        processor = new DnntAugmentProcessor();
    }

    @Test
    public void testProcessorOutput() {
        List<SolrInputDocument> inputDocs = Arrays.asList(
                new SolrInputDocument("id", "1"),
                new SolrInputDocument("id", "2"),
                new SolrInputDocument("id", "3")
        );
        List<SolrInputDocument> expectedOut = Arrays.asList(
                new SolrInputDocument("id", "1"),
                new SolrInputDocument("id", "2"),
                new SolrInputDocument("id", "3")
        );
        Map<String, Boolean> modifier = Collections.singletonMap("set", true);
        expectedOut.forEach(d -> d.addField(processor.DNNT_FIELD_NAME, modifier));

        List<SolrInputDocument> actualOut = processor.process(inputDocs);
        assertNotNull(actualOut);
        actualOut.forEach(d -> {
            assertTrue(d.getFieldNames().contains(processor.TIMESTAMP_FIELD_NAME));
            d.removeField(processor.TIMESTAMP_FIELD_NAME);
        });
        assertIterableEquals(expectedOut, actualOut);
    }
}
