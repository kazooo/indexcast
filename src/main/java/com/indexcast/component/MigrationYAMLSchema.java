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

    @JsonProperty(value = "unique_key", required = true)
    private String uniqueKey;

    @JsonProperty(required = false)
    private Map<String, String> fields;

    @JsonProperty(value = "ignored_fields", required = false)
    private List<String> ignoredFields;

    @JsonProperty(required = false)
    private List<String> processors;

    private final List<String> requestFields;

    public MigrationYAMLSchema() {
        requestFields = new ArrayList<>();
    }

    public void setUpRequestFields() {
        if (fields == null) {
            requestFields.add("*");
        } else {
            requestFields.addAll(fields.keySet());
        }
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
        if (fields != null) {
            checkDocContainsSpecifiedFields(srcDocFieldNames);
        }

        SolrInputDocument inputDoc = new SolrInputDocument();
        for (String fieldName : srcDocFieldNames) {
            if (ignoredFields != null && ignoredFields.contains(fieldName)) continue;
            String newFieldName = fields != null ? fields.get(fieldName) : fieldName;
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
