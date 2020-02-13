package cz.mzk.model;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Aleksei Ermak
 */

public class MigrationYAMLSchema {

    public String uniqueKey;
    public Map<String, String> fields;
    public List<String> processors;

    public SolrInputDocument convert(SolrDocument doc) {
        SolrInputDocument inputDoc = new SolrInputDocument();
        Collection<String> srcDocFieldNames = doc.getFieldNames();
        for (String fieldName : srcDocFieldNames) {
            assert fields.containsKey(fieldName);
            String newFieldName = fields.get(fieldName);
            inputDoc.addField(newFieldName, doc.getFieldValue(fieldName));
        }
        return inputDoc;
    }
}
