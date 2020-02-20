package cz.mzk.configuration;

import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.solr.DstSolrClient;
import cz.mzk.solr.SrcSolrClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private JobLauncherTestUtils jobLauncherTestUtils;

    @Before
    public void setup() throws Exception {
        configuration.setDataSource(null);
        Job indexcastParallelJob = configuration.parallelStepsJob();

        ResourcelessTransactionManager transactionManager = new ResourcelessTransactionManager();

        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager);
        factory.setTransactionManager(transactionManager);
        JobRepository jobRepository = factory.getObject();

        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());

        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJobRepository(jobRepository);
        jobLauncherTestUtils.setJob(indexcastParallelJob);
    }

    @Test
    public void testJobInstanceName() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        assertEquals(actualJobInstance.getJobName(), "parallel-solr-migration");
    }
}
