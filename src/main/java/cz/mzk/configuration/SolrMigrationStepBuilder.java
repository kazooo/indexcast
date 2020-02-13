package cz.mzk.configuration;

import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.reader.SrcSolrDocReader;
import cz.mzk.writer.DstSolrDocWriter;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * @author Aleksei Ermak
 */

@Configuration
public class SolrMigrationStepBuilder {

    @Autowired
    CursorMarkGlobalStorage cursorMarkStorage;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    ProcessorComposer processorComposer;

    public Step build(String name) {
        return stepBuilderFactory.get(name)
                .<List<SolrInputDocument>, List<SolrInputDocument>>chunk(1)
                .reader(migrationReader())
                .processor(compositeProcessor())
                .writer(migrationWriter())
                .build();
    }

    private ItemReader<List<SolrInputDocument>> migrationReader() {
        return new SrcSolrDocReader(cursorMarkStorage);
    }

    private ItemWriter<List<SolrInputDocument>> migrationWriter() {
        return new DstSolrDocWriter();
    }

    public CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> compositeProcessor() {
        return processorComposer.composite();
    }
}
