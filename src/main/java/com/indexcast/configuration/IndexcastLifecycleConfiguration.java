package com.indexcast.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;


/**
 * Main configuration for Spring Batch project.
 * At start creates job constructed from parallel running steps.
 * One step is responsible for fetching cursor marks, another ones process and migrate Solr documents.
 *
 * @author Aleksei Ermak
 */

@Configuration
@EnableBatchProcessing
@Slf4j
@AllArgsConstructor
public class IndexcastLifecycleConfiguration extends DefaultBatchConfigurer {

    private final JobBuilderFactory jobBuilderFactory;
    private final CursorFetchingStepBuilder cursorFetchStepBuilder;
    private final MigrationStepBuilder migrationStepBuilder;
    private final IndexcastParameterConfiguration toolConfiguration;

    @Override
    public void setDataSource(DataSource dataSource) {
        //This BatchConfigurer ignores any DataSource
    }

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
        log.debug("Create " + threads + " migration flows and 1 cursor fetching flow.");

        return (jobBuilderFactory.get("parallel-solr-migration")
                .incrementer(new RunIdIncrementer())
                .start(mainFlow)
                .build()).build();
    }
}
