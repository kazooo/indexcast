package com.indexcast.configuration;

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
        "SRC_CORE_NAME=solr/src_test",
        "DST_CORE_NAME=solr/dst_test",
        "SRC_SOLR_HOST=no_host",
        "DST_SOLR_HOST=no_host",
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml",
        "WAIT_IF_SOLR_FAIL=45000"
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
        assertEquals(configuration.getSrcCoreName(), "solr/src_test");
        assertEquals(configuration.getDstCoreName(), "solr/dst_test");
        assertEquals(configuration.getDocsPerCycle(), 5000);
        assertEquals(configuration.getQuery(), "*:*");
        assertNotNull(configuration.getMigrationYAMLSchema());
        assertEquals(configuration.getProcessorClassNames(), Collections.singletonList("FakeProcessor"));
        assertEquals(configuration.getWaitMillisIfSolrFail(), 45000);
    }
}
