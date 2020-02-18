package cz.mzk.configuration;

import cz.mzk.component.CursorMarkGlobalStorage;
import cz.mzk.reader.SrcSolrDocReader;
import cz.mzk.solr.DstSolrClient;
import cz.mzk.solr.SrcSolrClient;
import cz.mzk.writer.DstSolrDocWriter;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

@Configuration
public class MigrationStepBuilder {

    private final StepBuilderFactory stepBuilderFactory;
    private final ProcessorAutoComposer processorComposer;
    private final IndexcastParameterConfiguration toolParameterConfiguration;
    private final CursorMarkGlobalStorage cursorMarkGlobalStorage;
    private final SrcSolrClient srcSolrClient;
    private final DstSolrClient dstSolrClient;

    public MigrationStepBuilder(StepBuilderFactory stepBuilderFactory,
                                ProcessorAutoComposer processorComposer,
                                IndexcastParameterConfiguration toolParameterConfiguration,
                                CursorMarkGlobalStorage cursorMarkGlobalStorage,
                                SrcSolrClient srcSolrClient, DstSolrClient dstSolrClient) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.processorComposer = processorComposer;
        this.toolParameterConfiguration = toolParameterConfiguration;
        this.cursorMarkGlobalStorage = cursorMarkGlobalStorage;
        this.srcSolrClient = srcSolrClient;
        this.dstSolrClient = dstSolrClient;
    }

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
        return processorComposer.composite();
    }
}
