package cz.mzk.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * @author Aleksei Ermak
 */

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    CursorFetchingStepBuilder cursorFetchStepBuilder;

    @Autowired
    SolrMigrationStepBuilder migrationStepBuilder;

    @Autowired
    MigrationToolConfiguration toolConfiguration;

    private final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Bean
    public Job parallelStepsJob() {

        Flow cursorFetchFlow = new FlowBuilder<Flow>("cursor-fetch")
                .start(cursorFetchStepBuilder.build("cursor-fetch"))
                .build();

        Flow migrationFlow = new FlowBuilder<Flow>("migration-1")
                .start(migrationStepBuilder.build("migration-1"))
                .build();

        Flow mainFlow = new FlowBuilder<Flow>("main-flow")
                .split(new SimpleAsyncTaskExecutor()).add(cursorFetchFlow, migrationFlow).build();

        return (jobBuilderFactory.get("parallel-solr-migration")
                .incrementer(new RunIdIncrementer())
                .start(mainFlow)
                .build()).build();

    }
}
