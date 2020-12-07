package com.indexcast.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public SrcSolrClient(@Qualifier("src_solr") SolrClient srcSolrClient,
                         @Value("#{indexcastParameterConfiguration.srcSolrHost}") String srcSolrHost,
                         @Value("#{indexcastParameterConfiguration.srcCoreName}") String coreName,
                         @Value("#{indexcastParameterConfiguration.waitMillisIfSolrFail}") int waitIfSolrFail) {
        super(srcSolrHost, srcSolrClient, coreName, waitIfSolrFail);
    }

    @PreDestroy
    public void onDestroy() {
        super.close();
    }
}
