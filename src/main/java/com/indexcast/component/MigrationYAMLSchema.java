package com.indexcast.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This class is used to save migration schema properties.
 * Also it is used to convert Solr documents to ready-to-index Solr input documents.
 *
 * @author Aleksei Ermak
 */

public class MigrationYAMLSchema {

    @JsonProperty(required = true)
    private String uniqueKey;

    @JsonProperty(required = true)
    private Map<String, String> fields;

    @JsonProperty(required = false)
    private List<String> processors;

    private List<String> requestFields;

    public MigrationYAMLSchema() {
        requestFields = new ArrayList<>();
    }

    public void setUpRequestFields() {
        requestFields.addAll(fields.keySet());
    }

    /**
     * Checks original Solr document fields and convert it to ready-to-index Solr input document.
     * Maps document fields to fields specified by migration schema.
     *
     * @param doc  original Solr document from source Solr instance
     * @return     ready-to-index Solr input document with migrated fields
     */
    public SolrInputDocument convert(SolrDocument doc) {
        Collection<String> srcDocFieldNames = doc.getFieldNames();
        checkDocContainsSpecifiedFields(srcDocFieldNames);
        checkDocHasOnlySpecifiedFields(srcDocFieldNames);

        SolrInputDocument inputDoc = new SolrInputDocument();
        for (String fieldName : srcDocFieldNames) {
            String newFieldName = fields.get(fieldName);
            inputDoc.addField(newFieldName, doc.getFieldValue(fieldName));
        }
        return inputDoc;
    }

    private void checkDocContainsSpecifiedFields(Collection<String> srcDocFieldNames) {
        for (String fieldName : fields.keySet()) {
            if (!srcDocFieldNames.contains(fieldName)) {
                throw new IllegalStateException("Solr document doesn't have field \""
                        + fieldName + "\" specified in migration schema!");
            }
        }
    }

    private void checkDocHasOnlySpecifiedFields(Collection<String> srcDocFieldNames) {
        for (String fieldName : srcDocFieldNames) {
            if (!fields.containsKey(fieldName)) {
                throw new IllegalStateException("Solr document contains field \""
                        + fieldName + "\" not specified in migration schema!");
            }
        }
    }

    public List<String> getProcessorNames() {
        return processors;
    }

    public List<String> getRequestFields() {
        return requestFields;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }
}
