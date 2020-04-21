package com.indexcast.processor;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestDebugLoggingProcessorTest {

    private DebugLoggingProcessor processor;

    @Before
    public void init() {
        processor = new DebugLoggingProcessor();
    }

    @Test
    public void testProcessDoingNothing() {
        List<SolrInputDocument> inputDocs = Arrays.asList(
                new SolrInputDocument(),
                new SolrInputDocument(),
                new SolrInputDocument()
        );
        List<SolrInputDocument> outputDocs = processor.process(inputDocs);
        assertEquals(inputDocs, outputDocs);
    }
}
