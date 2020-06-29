package com.indexcast.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MigrationYAMLSchemaTest {

    private MigrationYAMLSchema simpleSchema;
    private MigrationYAMLSchema noFieldsSchema;

    @Before
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String simpleYamlConfigFilePath = "src/test/resources/migration-test-schema.yml";
        simpleSchema = mapper.readValue(new File(simpleYamlConfigFilePath), MigrationYAMLSchema.class);
        simpleSchema.setUpRequestFields();
        String noFieldsYamlConfigFilePath = "src/test/resources/migration-test-schema-no-fields.yml";
        noFieldsSchema = mapper.readValue(new File(noFieldsYamlConfigFilePath), MigrationYAMLSchema.class);
        noFieldsSchema.setUpRequestFields();
    }

    @Test
    public void testSchemaProcessors() {
        List<String> processorNames = simpleSchema.getProcessors();
        assertEquals(processorNames, Collections.singletonList("FakeProcessor"));
    }

    @Test
    public void testRequestFields() {
        List<String> fieldNames = simpleSchema.getRequestFields();
        assertEquals(fieldNames, Arrays.asList("id", "title"));
    }

    @Test
    public void testAllRequestFields() {
        List<String> fieldNames = noFieldsSchema.getRequestFields();
        assertEquals(fieldNames, Collections.singletonList("*"));
    }

    @Test
    public void testUniqueKey() {
        String uniqueKey = simpleSchema.getUniqueKey();
        assertEquals(uniqueKey, "id");
    }

    @Test(expected = IllegalStateException.class)
    public void testSolrDocumentNonSpecifiedFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("text", "doc_text");
        simpleSchema.convert(doc);
    }

    @Test
    public void testSolrDocumentNonSpecifiedFieldConversionOK() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("text", "doc_text");
        SolrInputDocument inputDoc = noFieldsSchema.convert(doc);
        assertTrue(inputDoc.getFieldNames().containsAll(Arrays.asList("id", "text")));
    }

    @Test
    public void testSolrDocumentAdditionalFieldConversionOK() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        doc.addField("text", "doc_text");
        SolrInputDocument inputDoc = noFieldsSchema.convert(doc);
        assertTrue(inputDoc.getFieldNames().containsAll(Arrays.asList("id", "title", "text")));
    }

    @Test
    public void testSolrDocumentSuccessfulConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        SolrInputDocument inputDoc = simpleSchema.convert(doc);
        assertTrue(inputDoc.getFieldNames().containsAll(Arrays.asList("id", "title")));
    }

    @Test
    public void testSolrDocumentIgnoredFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        doc.addField("version", "doc_version");
        SolrInputDocument inputDoc = noFieldsSchema.convert(doc);
        assertTrue(inputDoc.getFieldNames().containsAll(Arrays.asList("id", "title")));
    }
}
