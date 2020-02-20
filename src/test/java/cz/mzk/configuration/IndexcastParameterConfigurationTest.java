package cz.mzk.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        IndexcastParameterConfiguration.class,
})
@TestPropertySource(properties = {
        "THREADS=3",
        "QUERY=*:*",
        "PER_CYCLE=5000",
        "CORE_NAME=solr/test",
        "SRC_SOLR_HOST=no_host",
        "DST_SOLR_HOST=no_host",
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml"
})
public class IndexcastParameterConfigurationTest {

    @Autowired
    IndexcastParameterConfiguration configuration;

    @Test
    public void testConfiguration() {
        assertEquals(configuration.getThreads(), 3);
        assertEquals(configuration.getSrcSolrHost(), "no_host");
        assertEquals(configuration.getDstSolrHost(), "no_host");
        assertEquals(configuration.getUniqKey(), "id");
        assertEquals(configuration.getCoreName(), "solr/test");
        assertEquals(configuration.getDocsPerCycle(), 5000);
        assertEquals(configuration.getQuery(), "*:*");
        assertNotNull(configuration.getMigrationYAMLSchema());
        assertEquals(configuration.getProcessorClassNames(), Collections.singletonList("FakeProcessor"));
    }
}
