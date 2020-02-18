package cz.mzk.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.mzk.component.MigrationYAMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * @author Aleksei Ermak
 */

@Configuration
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

    private String coreName = "solr/kramerius";
    private MigrationYAMLSchema migrationYAMLSchema;

    private final Logger logger = LoggerFactory.getLogger(IndexcastParameterConfiguration.class);

    @PostConstruct
    public void postInit() throws IOException {
        File file = new File(pathToSchema);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        migrationYAMLSchema = mapper.readValue(file, MigrationYAMLSchema.class);
        migrationYAMLSchema.setUpRequestFields();

        logger.info("                Query: " + query);
        logger.info("              Threads: " + threads);
        logger.info("  Documents per cycle: " + docsPerCycle);
        logger.info("       Path to schema: " + pathToSchema);
        logger.info("     Source Solr host: " + srcSolrHost);
        logger.info("Destination Solr host: " + dstSolrHost);
    }

    public List<String> getProcessorClassNames() {
        return migrationYAMLSchema.getProcessorNames();
    }

    public int getThreads() {
        return threads;
    }

    public int getDocsPerCycle() {
        return docsPerCycle;
    }

    public String getDstSolrHost() {
        return dstSolrHost;
    }

    public String getSrcSolrHost() {
        return srcSolrHost;
    }

    public String getQuery() {
        return query;
    }

    public MigrationYAMLSchema getMigrationYAMLSchema() {
        return migrationYAMLSchema;
    }

    public String getCoreName() {
        return coreName;
    }

    public String getUniqKey() {
        return migrationYAMLSchema.getUniqueKey();
    }
}
