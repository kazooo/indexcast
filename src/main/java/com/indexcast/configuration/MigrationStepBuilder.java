package com.indexcast.configuration;

import com.indexcast.component.CursorMarkGlobalStorage;
import com.indexcast.reader.SrcSolrDocReader;
import com.indexcast.solr.DstSolrClient;
import com.indexcast.solr.SrcSolrClient;
import com.indexcast.writer.DstSolrDocWriter;
import lombok.AllArgsConstructor;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * This builder creates Spring Batch step responsible for Solr document
 * processing and migration from source Solr instance to destination Solr instance.
 *
 * @author Aleksei Ermak
 */

@Configuration
@AllArgsConstructor
public class MigrationStepBuilder {

    private final StepBuilderFactory stepBuilderFactory;
    private final ProcessorAutoComposer processorComposer;
    private final IndexcastParameterConfiguration toolParameterConfiguration;
    private final CursorMarkGlobalStorage cursorMarkGlobalStorage;
    private final SrcSolrClient srcSolrClient;
    private final DstSolrClient dstSolrClient;

    public Step build(String name) {
        // until reader can get cursors and doesnt return null, jobs will run in infinite loop
        return stepBuilderFactory.get(name)
                .<List<SolrInputDocument>, List<SolrInputDocument>>chunk(1)  // process only 1 batch
                .reader(migrationReader())
                .processor(compositeProcessor())
                .writer(migrationWriter())
                .build();
    }

    private ItemReader<List<SolrInputDocument>> migrationReader() {
        return new SrcSolrDocReader(toolParameterConfiguration, cursorMarkGlobalStorage, srcSolrClient);
    }

    private ItemWriter<List<SolrInputDocument>> migrationWriter() {
        return new DstSolrDocWriter(dstSolrClient);
    }

    public CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> compositeProcessor() {
        return processorComposer.compose();
    }
}
