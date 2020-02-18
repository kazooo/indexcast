package cz.mzk.configuration;

import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.solr.DstSolrClient;
import cz.mzk.solr.SrcSolrClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
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
        "SRC_SOLR_HOST=no_host",
        "DST_SOLR_HOST=no_host",
        "SCHEMA_PATH=src/test/resources/migration-test-schema.yml"
})
public class StepBuilderIntegrationTest {

    @Autowired
    private CursorFetchingStepBuilder cursorFetchingStepBuilder;

    @Autowired
    private MigrationStepBuilder migrationStepBuilder;

    @Test
    public void testCursorFetchingStepBuild() {
        Step step = cursorFetchingStepBuilder.build("test-cursor-fetching");
        assertNotNull(step);
        assertEquals(step.getName(), "test-cursor-fetching");
    }

    @Test
    public void testMigrationStepBuild() {
        Step step = migrationStepBuilder.build("test-migration");
        assertNotNull(step);
        assertEquals(step.getName(), "test-migration");
    }
}

