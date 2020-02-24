package cz.mzk.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.component.MigrationYAMLSchema;
import cz.mzk.component.Pair;
import cz.mzk.configuration.IndexcastParameterConfiguration;
import cz.mzk.solr.SrcSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SrcSolrDocReaderTest {

    @Mock
    SrcSolrClient solrClient;

    @Mock
    IndexcastParameterConfiguration configuration;

    @Mock
    CursorMarkGlobalStorage storage;

    @Test
    public void testRead() throws IOException {
        setupMocks();
        SrcSolrDocReader reader = new SrcSolrDocReader(configuration, storage, solrClient);

        List<SolrInputDocument> inputDocs = reader.read();
        assertNotNull(inputDocs);
        assertEquals(inputDocs.size(), 2);
        assertTrue(docsAreOk(inputDocs));
        verify(storage, times(1)).getNextCursorAndObjNum();
        verify(solrClient, times(1)).queryWithCursor(any());

        inputDocs = reader.read();
        assertNotNull(inputDocs);
        assertEquals(inputDocs.size(), 2);
        assertTrue(docsAreOk(inputDocs));
        verify(storage, times(2)).getNextCursorAndObjNum();
        verify(solrClient, times(2)).queryWithCursor(any());

        inputDocs = reader.read();
        assertNull(inputDocs);
        verify(storage, times(3)).getNextCursorAndObjNum();
        verify(solrClient, times(2)).queryWithCursor(any());
    }

    private void setupMocks() throws IOException {
        when(configuration.getUniqKey()).thenReturn("id");
        when(configuration.getQuery()).thenReturn("*:*");
        when(configuration.getMigrationYAMLSchema()).thenReturn(createSchema());

        when(storage.getNextCursorAndObjNum())
                .thenReturn(new Pair<>("*", 2))
                .thenReturn(new Pair<>("cursor", 2))
                .thenReturn(null);

        when(solrClient.queryWithCursor(any(SolrQuery.class))).thenReturn(createSolrResponse());
    }

    private MigrationYAMLSchema createSchema() throws IOException {
        File file = new File("src/test/resources/migration-test-schema.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MigrationYAMLSchema schema = mapper.readValue(file, MigrationYAMLSchema.class);
        schema.setUpRequestFields();
        return schema;
    }

    private Pair<String, SolrDocumentList> createSolrResponse() {
        String nextCursor = "ignored_next_cursor";
        SolrDocumentList docs = new SolrDocumentList();
        docs.add(createDoc());
        docs.add(createDoc());
        return new Pair<>(nextCursor, docs);
    }

    private SolrDocument createDoc() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        return doc;
    }

    private boolean docsAreOk(List<SolrInputDocument> docs) {
        for (SolrInputDocument doc : docs) {
            List<String> fieldNames = new ArrayList<>(doc.getFieldNames());
            if (!Arrays.asList("id", "title").equals(fieldNames))
                return false;
        }
        return true;
    }
}
