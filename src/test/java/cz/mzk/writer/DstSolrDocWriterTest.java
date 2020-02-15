package cz.mzk.writer;

import cz.mzk.solr.DstSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author Aleksei Ermak
 */

@RunWith(MockitoJUnitRunner.class)
public class DstSolrDocWriterTest {

    @Mock
    DstSolrClient solrClient;

    @Test
    public void testWrite() {
        DstSolrDocWriter writer = new DstSolrDocWriter(solrClient);
        writer.write(Arrays.asList(
                Arrays.asList(
                        new SolrInputDocument(),
                        new SolrInputDocument()),
                Arrays.asList(
                        new SolrInputDocument(),
                        new SolrInputDocument())
        ));
        verify(solrClient, times(4)).index(any(SolrInputDocument.class));
        verify(solrClient, times(1)).commit();
    }
}
