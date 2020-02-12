package cz.mzk.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.mzk.model.MigrationYAMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;


/**
 * @author Aleksei Ermak
 */

@Component
public class MigrationToolConfiguration {

    @Value("${THREADS:4}")
    public int threads;

    @Value("${QUERY:*:*}")
    public String query;

    @Value("${PER_CYCLE:5000}")
    public int docsPerCycle;

    @Value("${SRC_SOLR_HOST:null}")
    public String srcSolrHost;

    @Value("${DST_SOLR_HOST:null}")
    public String dstSolrHost;

    @Value("${COMPOSITE:false}")
    public boolean useCompositeID;

    @Value("${SCHEMA_PATH}")
    public String pathToSchema;

    private MigrationYAMLSchema migrationYAMLSchema;

    private final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @PostConstruct
    public void init() throws IOException {
        File file = new File(pathToSchema);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.migrationYAMLSchema = mapper.readValue(file, MigrationYAMLSchema.class);
    }
}
