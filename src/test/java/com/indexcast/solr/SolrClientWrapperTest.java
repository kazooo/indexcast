package com.indexcast.solr;

import com.indexcast.component.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class SolrClientWrapperTest {

    @Mock
    HttpSolrClient mockSolrClient;

    @Mock
    QueryResponse response;

    private SolrClientWrapper wrapper;
    private final int solrWaitIfFailed = 3000;

    @Before
    public void setup() {
        wrapper = new SolrClientWrapper(
                "no_host", mockSolrClient,
                "test-core", solrWaitIfFailed
        );
    }

    @Test(timeout = 1000)
    public void testSuccessfulQueryWithCursor() throws IOException, SolrServerException {
        SolrDocumentList fakeList = createFakeSolrDocList();
        when(response.getResults()).thenReturn(fakeList);
        when(response.getNextCursorMark()).thenReturn("cursor");
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenReturn(response);
        Pair<String, SolrDocumentList> wrapperResponse = wrapper.queryWithCursor(new SolrQuery());
        assertEquals(wrapperResponse.getKey(), "cursor");
        assertEquals(wrapperResponse.getValue(), fakeList);
    }

    @Test
    public void testFailedQueryWithCursorWaiting() throws IOException, SolrServerException, InterruptedException {
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenThrow(SolrServerException.class);
        threadWaitingSuccessfully(() -> wrapper.queryWithCursor(new SolrQuery()));
    }

    @Test(timeout = 1000)
    public void testSuccessfulQueryCursorAndNumFound() throws IOException, SolrServerException {
        SolrDocumentList fakeList = createFakeSolrDocList();
        when(response.getResults()).thenReturn(fakeList);
        when(response.getNextCursorMark()).thenReturn("cursor");
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenReturn(response);
        Pair<String, Integer> wrapperResponse = wrapper.queryCursorAndDocsToMigrate(new SolrQuery());
        assertEquals(wrapperResponse.getKey(), "cursor");
        assertEquals(2, (int) wrapperResponse.getValue());
    }

    @Test
    public void testFailedQueryCursorAndDocsToMigrateWaiting()
            throws InterruptedException, IOException, SolrServerException {
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenThrow(SolrServerException.class);
        threadWaitingSuccessfully(() -> wrapper.queryCursorAndDocsToMigrate(new SolrQuery()));
    }

    @Test(timeout = 1000)
    public void testSuccessfulIndex() {
        wrapper.index(new SolrInputDocument());
    }

    @Test
    public void testFailedIndexWaiting() throws IOException, SolrServerException, InterruptedException {
        when(mockSolrClient.add(any(String.class), any(SolrInputDocument.class)))
                .thenThrow(SolrServerException.class);
        threadWaitingSuccessfully(() -> wrapper.index(new SolrInputDocument()));
    }

    @Test
    public void testSuccessfulCommit() {
        wrapper.commit();
    }

    @Test
    public void testFailedCommit() throws IOException, SolrServerException {
        when(mockSolrClient.commit(any(String.class))).thenThrow(SolrServerException.class);
        wrapper.commit();
    }

    @Test
    public void testSuccessfulClose() {
        wrapper.close();
    }

    @Test
    public void testFailedClose() throws IOException {
        doThrow(IOException.class).when(mockSolrClient).close();
        wrapper.close();
    }

    @Test
    public void testOkPing() throws IOException, SolrServerException, InterruptedException {
        when(mockSolrClient.add(any(String.class), any(SolrInputDocument.class)))
                .thenThrow(SolrServerException.class);
        SolrPingResponse pingResponse = new SolrPingResponse();
        NamedList<Object> status = new NamedList<>(new HashMap<String, Integer>() {{
            put("status", 0);
        }});
        pingResponse.setResponse(status);
        when(mockSolrClient.ping(any())).thenReturn(pingResponse);
        threadWaitingSuccessfully(() -> wrapper.index(new SolrInputDocument()));
    }

    private SolrDocumentList createFakeSolrDocList() {
        SolrDocumentList docs = new SolrDocumentList();
        docs.add(createDoc());
        docs.add(createDoc());
        docs.setNumFound(2);
        return docs;
    }

    private SolrDocument createDoc() {
        SolrDocument doc = new SolrDocument();
        doc.addField("PID", "doc_pid");
        doc.addField("root", "doc_root");
        return doc;
    }

    private void threadWaitingSuccessfully(Runnable target) throws InterruptedException {
        Thread t = new Thread(target);
        t.start();
        Thread.sleep(solrWaitIfFailed + 1000); // wait to coverage waitForConnection
        assertTrue(t.isAlive());
        Thread.sleep(1000); // to trigger InterruptedException
        t.interrupt();
    }
}
