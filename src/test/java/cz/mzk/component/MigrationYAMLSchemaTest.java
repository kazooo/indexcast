package cz.mzk.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MigrationYAMLSchemaTest {

    private MigrationYAMLSchema schema;

    @Before
    public void init() throws IOException {
        File file = new File("src/test/resources/migration-test-schema.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        schema = mapper.readValue(file, MigrationYAMLSchema.class);
        schema.setUpRequestFields();
    }

    @Test
    public void testSchemaProcessors() {
        List<String> processorNames = schema.getProcessorNames();
        assertEquals(processorNames, Collections.singletonList("TestProcessor"));
    }

    @Test
    public void testRequestFields() {
        List<String> fieldNames = schema.getRequestFields();
        assertEquals(fieldNames, Arrays.asList("PID", "root"));
    }

    @Test
    public void testUniqueKey() {
        String uniqueKey = schema.getUniqueKey();
        assertEquals(uniqueKey, "PID");
    }

    @Test(expected = IllegalStateException.class)
    public void testSolrDocumentNonSpecifiedFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("PID", "doc_pid");
        doc.addField("text", "doc_text");
        schema.convert(doc);
    }

    @Test(expected = IllegalStateException.class)
    public void testSolrDocumentAdditionalFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("PID", "doc_pid");
        doc.addField("root", "doc_root");
        doc.addField("text", "doc_text");
        schema.convert(doc);
    }

    @Test
    public void testSolrDocumentSuccessfulConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("PID", "doc_pid");
        doc.addField("root", "doc_root");
        schema.convert(doc);
    }
}
