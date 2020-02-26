package com.indexcast.writer;

import com.indexcast.solr.DstSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DstSolrDocWriterTest {

    @Mock
    DstSolrClient solrClient;

    private List<List<SolrInputDocument>> dumbData = Arrays.asList(
            Arrays.asList(
                    new SolrInputDocument(),
                    new SolrInputDocument()),
            Arrays.asList(
                    new SolrInputDocument(),
                    new SolrInputDocument())
    );

    @Test
    public void testSuccessfulWrite() {
        DstSolrDocWriter writer = new DstSolrDocWriter(solrClient);
        writer.write(dumbData);
        verify(solrClient, times(4)).index(any(SolrInputDocument.class));
        verify(solrClient, times(1)).commit();
    }
}
