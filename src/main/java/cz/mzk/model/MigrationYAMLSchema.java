package cz.mzk.model;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Aleksei Ermak
 */

public class MigrationYAMLSchema {

    private String uniqueKey;
    private Map<String, String> fields;
    private List<String> processors;

    private List<String> requestFields;

    public MigrationYAMLSchema() {
        requestFields = new ArrayList<>();
    }

    public void setUpRequestFields() {
        requestFields.addAll(fields.keySet());
    }

    public SolrInputDocument convert(SolrDocument doc) {
        SolrInputDocument inputDoc = new SolrInputDocument();
        Collection<String> srcDocFieldNames = doc.getFieldNames();
        for (String fieldName : srcDocFieldNames) {
            if (fields.containsKey(fieldName)) {
                String newFieldName = fields.get(fieldName);
                inputDoc.addField(newFieldName, doc.getFieldValue(fieldName));
            } else {
                throw new IllegalStateException("Solr document contains fields not specified in migration schema!");
            }
        }
        return inputDoc;
    }

    public List<String> getProcessors() {
        return processors;
    }

    public List<String> getRequestFields() {
        return requestFields;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }
}
