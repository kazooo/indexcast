package cz.mzk.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


/**
 * Source Solr instance client configuring by Spring Boot autowiring.
 *
 * @author Aleksei Ermak
 */

@Component
public class SrcSolrClient extends SolrClientWrapper {

    @Autowired
    public SrcSolrClient(@Value("#{indexcastParameterConfiguration.srcSolrHost}") String srcSolrHost,
                         @Value("#{indexcastParameterConfiguration.srcCoreName}") String coreName) {
        super(srcSolrHost, coreName);
    }

    @PreDestroy
    public void onDestroy() {
        super.close();
    }
}
