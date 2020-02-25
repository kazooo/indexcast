package cz.mzk.solr;

import cz.mzk.component.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolrClientWrapperTest {

    @Mock
    HttpSolrClient mockSolrClient;

    @Mock
    QueryResponse response;

    private SolrClientWrapper wrapper;

    @Before
    public void setup() {
        wrapper = new SolrClientWrapper("no_host", "test-core");
        wrapper.setupCustomSolrClient(mockSolrClient);
    }

    @Test
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
    public void testFailedQueryWithCursor() throws IOException, SolrServerException {
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenThrow(SolrServerException.class);
        assertNull(wrapper.queryWithCursor(new SolrQuery()));
    }

    @Test
    public void testSuccessfulQueryCursorAndNumFound() throws IOException, SolrServerException {
        SolrDocumentList fakeList = createFakeSolrDocList();
        when(response.getResults()).thenReturn(fakeList);
        when(response.getNextCursorMark()).thenReturn("cursor");
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenReturn(response);
        Pair<String, Integer> wrapperResponse = wrapper.queryCursorAndNumFound(new SolrQuery());
        assertEquals(wrapperResponse.getKey(), "cursor");
        assertEquals(2, (int) wrapperResponse.getValue());
    }

    @Test
    public void testFailedQueryCursorAndNumFound() throws IOException, SolrServerException {
        when(mockSolrClient.query(any(String.class), any(SolrQuery.class)))
                .thenThrow(SolrServerException.class);
        assertNull(wrapper.queryCursorAndNumFound(new SolrQuery()));
    }

    @Test
    public void testSuccessfulIndex() {
        assertTrue(wrapper.index(new SolrInputDocument()));
    }

    @Test
    public void testFailedIndex() throws IOException, SolrServerException {
        when(mockSolrClient.add(any(String.class), any(SolrInputDocument.class)))
                .thenThrow(SolrServerException.class);
        assertFalse(wrapper.index(new SolrInputDocument()));
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
}
