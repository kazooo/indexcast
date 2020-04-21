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
import java.util.HashSet;
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
        assertEquals(processorNames, Collections.singletonList("FakeProcessor"));
    }

    @Test
    public void testRequestFields() {
        List<String> fieldNames = schema.getRequestFields();
        assertEquals(fieldNames, Arrays.asList("id", "title"));
    }

    @Test
    public void testUniqueKey() {
        String uniqueKey = schema.getUniqueKey();
        assertEquals(uniqueKey, "id");
    }

    @Test(expected = IllegalStateException.class)
    public void testSolrDocumentNonSpecifiedFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("text", "doc_text");
        doc.addField("_version_", "doc_version");
        schema.convert(doc);
    }

    @Test(expected = IllegalStateException.class)
    public void testSolrDocumentAdditionalFieldConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        doc.addField("text", "doc_text");
        doc.addField("_version_", "doc_version");
        schema.convert(doc);
    }

    @Test
    public void testSolrDocumentSuccessfulConversion() {
        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("title", "doc_title");
        doc.addField("_version_", "doc_version");
        schema.convert(doc);
    }

    @Test
    public void testSolrDocumentAllFieldsSuccessfulConversion() throws IOException {
        File file = new File("src/test/resources/migration-test-schema-all-fields.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        schema = mapper.readValue(file, MigrationYAMLSchema.class);
        schema.setUpRequestFields();

        List<String> fieldNames = schema.getRequestFields();
        assertEquals(fieldNames, Collections.singletonList("*"));

        SolrDocument doc = new SolrDocument();
        doc.addField("id", "doc_id");
        doc.addField("text", "doc_text");
        doc.addField("_version_", "doc_version");
        SolrInputDocument inputDoc = schema.convert(doc);
        assertEquals(inputDoc.keySet(), new HashSet<>(Arrays.asList("id", "text")));
    }
}
