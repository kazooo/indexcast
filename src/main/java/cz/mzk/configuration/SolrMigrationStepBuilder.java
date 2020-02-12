package cz.mzk.configuration;

import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.processor.ToInputDocProcessor;
import cz.mzk.reader.SrcSolrDocReader;
import cz.mzk.writer.DstSolrDocWriter;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
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

    public Step build(String name) {
        return stepBuilderFactory.get(name)
                .<SolrDocumentList, List<SolrInputDocument>>chunk(1)
                .reader(migrationReader())
                .processor(compositeProcessor())
                .writer(migrationWriter())
                .build();
    }

    private ItemReader<SolrDocumentList> migrationReader() {
        return new SrcSolrDocReader(cursorMarkStorage);
    }

    private ItemWriter<List<SolrInputDocument>> migrationWriter() {
        return new DstSolrDocWriter();
    }

    public CompositeItemProcessor<SolrDocumentList, List<SolrInputDocument>> compositeProcessor() {
        List<ItemProcessor<SolrDocumentList, List<SolrInputDocument>>> delegates = Arrays.asList(new ToInputDocProcessor());
        CompositeItemProcessor<SolrDocumentList, List<SolrInputDocument>> processor = new CompositeItemProcessor<>();
        processor.setDelegates(delegates);
        return processor;
    }
}
