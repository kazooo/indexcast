package cz.mzk.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @author Aleksei Ermak
 */

@Component
public class DstSolrClient extends SolrClientWrapper {

    @Autowired
    public DstSolrClient(@Value("#{indexcastParameterConfiguration.dstSolrHost}") String dstSolrHost,
                         @Value("#{indexcastParameterConfiguration.coreName}") String coreName) {
        super(dstSolrHost, coreName);
    }
}
