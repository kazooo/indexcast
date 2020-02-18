package cz.mzk.configuration;

import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.solr.DstSolrClient;
import cz.mzk.solr.SrcSolrClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        IndexcastLifecycleConfiguration.class,
        CursorFetchingStepBuilder.class,
        MigrationStepBuilder.class,
        ProcessorAutoComposer.class,
        IndexcastParameterConfiguration.class,
        CursorMarkGlobalStorage.class,
        StepBuilderFactory.class,
        SimpleJobRepository.class,
        ResourcelessTransactionManager.class,
        SrcSolrClient.class,
        DstSolrClient.class
})
@TestPropertySource(properties = {
        "CORE_NAME=solr/test",
        "SRC_SOLR_HOST=no_host",
        "DST_SOLR_HOST=no_host",
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml"
})
public class IndexcastLifecycleIntegrationTest {

    @Autowired
    private IndexcastLifecycleConfiguration configuration;

    @Test
    public void testJobCreating() {
        Job job = configuration.parallelStepsJob();
        assertNotNull(job);
        assertEquals(job.getName(), "parallel-solr-migration");
    }
}
