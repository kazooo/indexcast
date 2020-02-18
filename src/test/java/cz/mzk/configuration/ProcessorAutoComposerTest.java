package cz.mzk.configuration;

import cz.mzk.processor.FakeProcessor;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.support.CompositeItemProcessor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorAutoComposerTest {

    @Mock
    private IndexcastParameterConfiguration configuration;

    private ProcessorAutoComposer composer;

    @Before
    public void setup() {
        composer = new ProcessorAutoComposer(configuration);
    }

    @Test
    public void testSuccessfulLoading() throws Exception {
        when(configuration.getProcessorClassNames()).thenReturn(Collections.singletonList("FakeProcessor"));
        CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> processor = composer.composite();
        assertNotNull(processor);
        assertTrue(processorHasBeenLoaded("cz.mzk.processor.FakeProcessor"));
    }

    @Test
    public void testNoProcessors() {
        when(configuration.getProcessorClassNames()).thenReturn(null);
        assertNull(composer.composite());
    }

    public boolean processorHasBeenLoaded(String name) throws Exception {
        java.lang.reflect.Method m = ClassLoader.class
                .getDeclaredMethod("findLoadedClass", String.class);
        m.setAccessible(true);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Object test = m.invoke(cl, name);
        return test != null;
    }
}
