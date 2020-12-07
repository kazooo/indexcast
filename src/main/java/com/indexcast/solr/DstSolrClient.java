package com.indexcast.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public DstSolrClient(@Qualifier("dst_solr") SolrClient dstSolrClient,
                         @Value("#{indexcastParameterConfiguration.dstSolrHost}") String dstSolrHost,
                         @Value("#{indexcastParameterConfiguration.dstCoreName}") String coreName,
                         @Value("#{indexcastParameterConfiguration.waitMillisIfSolrFail}") int waitIfSolrFail) {
        super(dstSolrHost, dstSolrClient, coreName, waitIfSolrFail);
    }

    @PreDestroy
    public void onDestroy() {
        super.close();
    }
}
