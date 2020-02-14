package cz.mzk.configuration;

import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.reader.SrcSolrCursorReader;
import cz.mzk.solr.SrcSolrClient;
import cz.mzk.writer.CursorStorageWriter;
import javafx.util.Pair;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


/**
 * @author Aleksei Ermak
 */

@Configuration
public class CursorFetchingStepBuilder {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    ToolParameterConfiguration toolParameterConfiguration;

    @Autowired
    CursorMarkGlobalStorage cursorMarkGlobalStorage;

    @Autowired
    SrcSolrClient solrClient;

    public Step build(String name) {
        // until reader can fetch cursors and doesnt return null, job can run in infinite loop
        return stepBuilderFactory.get(name)
                .<Pair<String, Integer>, Pair<String, Integer>>chunk(1)  // read only 1 cursor then go to writing step
                .reader(fetchReader())
                .writer(fetchWriter())
                .build();
    }

    private ItemReader<Pair<String, Integer>> fetchReader() {
        return new SrcSolrCursorReader(toolParameterConfiguration, cursorMarkGlobalStorage, solrClient);
    }

    private ItemWriter<Pair<String, Integer>> fetchWriter() {
        return new CursorStorageWriter(cursorMarkGlobalStorage);
    }
}
