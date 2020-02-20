package cz.mzk.writer;

import cz.mzk.solr.DstSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * @author Aleksei Ermak
 */

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
        when(solrClient.index(any())).thenReturn(true);
        DstSolrDocWriter writer = new DstSolrDocWriter(solrClient);
        writer.write(dumbData);
        verify(solrClient, times(4)).index(any(SolrInputDocument.class));
        verify(solrClient, times(1)).commit();
    }

    @Test
    public void testWriteWithBadConnection() throws InterruptedException {
        DstSolrDocWriter writer = new DstSolrDocWriter(solrClient);
        Thread t = new Thread(() -> writer.write(dumbData));

        when(solrClient.index(any())).thenReturn(false);
        t.start();
        Thread.sleep(61000); // wait 1 minute to coverage waitForConnection
        assertTrue(t.isAlive());
        Thread.sleep(3000); // wait 3 sec to trigger InterruptedException
        t.interrupt();
    }
}
