package com.indexcast.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.indexcast.component.MigrationYAMLSchema;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private MigrationYAMLSchema migrationYAMLSchema;

    private final Logger logger = LoggerFactory.getLogger(IndexcastParameterConfiguration.class);

    @PostConstruct
    public void postInit() throws IOException {
        File file = new File(pathToSchema);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        migrationYAMLSchema = mapper.readValue(file, MigrationYAMLSchema.class);
        migrationYAMLSchema.setUpRequestFields();

        logger.info("                     Query: " + query);
        logger.info("                   Threads: " + threads);
        logger.info("       Documents per cycle: " + docsPerCycle);
        logger.info("            Path to schema: " + pathToSchema);
        logger.info("          Source Solr host: " + srcSolrHost);
        logger.info("     Source Solr core name: " + srcCoreName);
        logger.info("     Destination Solr host: " + dstSolrHost);
        logger.info("Destination Solr core name: " + dstCoreName);
    }

    public List<String> getProcessorClassNames() {
        return migrationYAMLSchema.getProcessors();
    }

    public String getUniqKey() {
        return migrationYAMLSchema.getUniqueKey();
    }
}
