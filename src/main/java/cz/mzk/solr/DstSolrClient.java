package cz.mzk.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DstSolrClient extends SolrClientWrapper {

    @Autowired
    public DstSolrClient(@Value("#{toolParameterConfiguration.dstSolrHost}") String dstSolrHost,
                         @Value("#{toolParameterConfiguration.coreName}") String coreName) {
        super(dstSolrHost, coreName);
    }
}
