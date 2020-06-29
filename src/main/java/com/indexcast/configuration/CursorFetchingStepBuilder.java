package com.indexcast.configuration;

import com.indexcast.component.Pair;
import com.indexcast.component.CursorMarkGlobalStorage;
import com.indexcast.reader.SrcSolrCursorReader;
import com.indexcast.solr.SrcSolrClient;
import com.indexcast.writer.CursorStorageWriter;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Configuration;


/**
 * This builder creates Spring Batch step responsible for cursor marks fetching from source Solr instance.
 *
 * @author Aleksei Ermak
 */

@Configuration
@AllArgsConstructor
public class CursorFetchingStepBuilder {

    private final StepBuilderFactory stepBuilderFactory;
    private final IndexcastParameterConfiguration toolParameterConfiguration;
    private final CursorMarkGlobalStorage cursorMarkGlobalStorage;
    private final SrcSolrClient solrClient;

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
