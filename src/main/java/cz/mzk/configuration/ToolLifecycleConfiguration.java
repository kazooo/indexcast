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
public class ToolLifecycleConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    CursorFetchingStepBuilder cursorFetchStepBuilder;

    @Autowired
    MigrationStepBuilder migrationStepBuilder;

    @Autowired
    ToolParameterConfiguration toolConfiguration;

    private final Logger logger = LoggerFactory.getLogger(ToolLifecycleConfiguration.class);

    @Bean
    public Job parallelStepsJob() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("main-flow");
        FlowBuilder.SplitBuilder<Flow> splitBuilder = flowBuilder.split(new SimpleAsyncTaskExecutor());

        Flow cursorFetchFlow = new FlowBuilder<Flow>("cursor-fetch")
                .start(cursorFetchStepBuilder.build("cursor-fetch"))
                .build();
        splitBuilder.add(cursorFetchFlow);


        int threads = toolConfiguration.getThreads();
        for (int i = 0; i < threads; i++) {
            Flow migrationFlow = new FlowBuilder<Flow>("migration-" + i)
                    .start(migrationStepBuilder.build("migration-" + i))
                    .build();
            splitBuilder.add(migrationFlow);
        }

        Flow mainFlow = flowBuilder.build();
        logger.debug("Create " + threads + " migration flows and 1 cursor fetching flows.");

        return (jobBuilderFactory.get("parallel-solr-migration")
                .incrementer(new RunIdIncrementer())
                .start(mainFlow)
                .build()).build();

    }
}
