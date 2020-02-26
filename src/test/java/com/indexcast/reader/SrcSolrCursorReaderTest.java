package com.indexcast.reader;

import com.indexcast.component.Pair;
import com.indexcast.configuration.IndexcastParameterConfiguration;
import com.indexcast.component.CursorMarkGlobalStorage;
import com.indexcast.solr.SrcSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SrcSolrCursorReaderTest {

    @Mock
    SrcSolrClient solrClient;

    @Mock
    IndexcastParameterConfiguration configuration;

    @Mock
    CursorMarkGlobalStorage storage;

    SrcSolrCursorReader reader;

    @Before
    public void setup() {
        reader = new SrcSolrCursorReader(configuration, storage, solrClient);
    }

    private void setupMocks() {
        when(configuration.getUniqKey()).thenReturn("PID");
        when(configuration.getQuery()).thenReturn("*:*");
        when(configuration.getDocsPerCycle()).thenReturn(5000);
    }

    @Test
    public void testReadCursor() {
        setupMocks();
        when(solrClient.queryCursorAndDocsToMigrate(any(SolrQuery.class)))
                .thenReturn(new Pair<>("cursor", 5000));
        Pair<String, Integer> response = reader.read();
        assertNotNull(response);
        assertEquals(response.getKey(), "*");
        assertEquals((int) response.getValue(), 5000);
    }

    @Test
    public void testGotNull() {
        setupMocks();
        when(solrClient.queryCursorAndDocsToMigrate(any(SolrQuery.class)))
                .thenReturn(new Pair<>("*", 0));
        Pair<String, Integer> response = reader.read();
        assertNull(response);
        verify(storage).close();
    }
}
