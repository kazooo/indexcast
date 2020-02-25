package com.indexcast.solr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


/**
 * Destination Solr instance client configuring by Spring Boot autowiring.
 *
 * @author Aleksei Ermak
 */

@Component
public class DstSolrClient extends SolrClientWrapper {

    @Autowired
    public DstSolrClient(@Value("#{indexcastParameterConfiguration.dstSolrHost}") String dstSolrHost,
                         @Value("#{indexcastParameterConfiguration.dstCoreName}") String coreName) {
        super(dstSolrHost, coreName);
    }

    @PreDestroy
    public void onDestroy() {
        super.close();
    }
}
