package cz.mzk.configuration;

import cz.mzk.model.CursorMarkGlobalStorage;
import cz.mzk.reader.SrcSolrCursorReader;
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
    CursorMarkGlobalStorage cursorMarkStorage;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    public Step build(String name) {
        return stepBuilderFactory.get(name)
                .<Pair<String, Integer>, Pair<String, Integer>>chunk(1)
                .reader(fetchReader())
                .writer(fetchWriter())
                .build();
    }

    private ItemReader<Pair<String, Integer>> fetchReader() {
        return new SrcSolrCursorReader();
    }

    private ItemWriter<Pair<String, Integer>> fetchWriter() {
        return new CursorStorageWriter(cursorMarkStorage);
    }
}
