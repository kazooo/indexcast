package cz.mzk.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @author Aleksei Ermak
 */

@Component
public class SrcSolrClient extends SolrClientWrapper {

    @Autowired
    public SrcSolrClient(@Value("#{indexcastParameterConfiguration.srcSolrHost}") String srcSolrHost,
                         @Value("#{indexcastParameterConfiguration.coreName}") String coreName) {
        super(srcSolrHost, coreName);
    }
}
