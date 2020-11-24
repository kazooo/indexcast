package com.indexcast.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.indexcast.component.MigrationYAMLSchema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Configuration to keep Indexcast application parameters.
 * Loads and initializes migration schema and reads parameters from system properties.
 *
 * @author Aleksei Ermak
 */

@Configuration
@Getter
@Slf4j
public class IndexcastParameterConfiguration {

    @Value("${THREADS:4}")
    private int threads;

    @Value("${QUERY:*:*}")
    private String query;

    @Value("${PER_CYCLE:5000}")
    private int docsPerCycle;

    @Value("${SRC_SOLR_HOST}")
    private String srcSolrHost;

    @Value("${DST_SOLR_HOST}")
    private String dstSolrHost;

    @Value("${SCHEMA_PATH}")
    private String pathToSchema;

    @Value("${SRC_CORE_NAME}")
    private String srcCoreName;

    @Value("${DST_CORE_NAME}")
    private String dstCoreName;

    @Value("${WAIT_IF_SOLR_FAIL:60000}")
    private int waitMillisIfSolrFail;

    @Value("${STORAGE_SIZE:20}")
    private int storageSize;

    private MigrationYAMLSchema migrationYAMLSchema;

    @PostConstruct
    public void postInit() throws IOException {
        File file = new File(pathToSchema);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        migrationYAMLSchema = mapper.readValue(file, MigrationYAMLSchema.class);
        migrationYAMLSchema.setUpRequestFields();

        log.info("                     Query: " + query);
        log.info("                   Threads: " + threads);
        log.info("       Documents per cycle: " + docsPerCycle);
        log.info("            Path to schema: " + pathToSchema);
        log.info("          Source Solr host: " + srcSolrHost);
        log.info("     Source Solr core name: " + srcCoreName);
        log.info("     Destination Solr host: " + dstSolrHost);
        log.info("Destination Solr core name: " + dstCoreName);
        log.info("  Cursor mark storage size: " + storageSize);
    }

    public List<String> getProcessorClassNames() {
        return migrationYAMLSchema.getProcessors();
    }

    public String getUniqKey() {
        return migrationYAMLSchema.getUniqueKey();
    }

    @Bean
    @Qualifier("src_solr")
    public SolrClient getSrcSolrClient() {
        return configureSolrClient(srcSolrHost);
    }

    @Bean
    @Qualifier("dst_solr")
    public SolrClient getDstSolrClient() {
        return configureSolrClient(dstSolrHost);
    }

    private SolrClient configureSolrClient(String url) {
        return new HttpSolrClient.Builder(url)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }
}
