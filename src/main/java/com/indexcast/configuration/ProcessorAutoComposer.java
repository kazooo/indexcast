package com.indexcast.configuration;

import com.indexcast.processor.ProcessorInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * This composer is used to load custom processors from processor package and composites them.
 * If no processors have been specified by migration schema, returns empty CompositeProcessor object.
 *
 * @author Aleksei Ermak
 */

@Component
@Slf4j
@AllArgsConstructor
public class ProcessorAutoComposer {

    private final IndexcastParameterConfiguration toolConfiguration;
    private final String packageName = "com.indexcast.processor";

    public CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> composite() {
        List<String> processorNames = toolConfiguration.getProcessorClassNames();
        if (processorNames == null) {
            return null;             // no processors have been specified, keep migrate without them
        }

        CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> compositeProcessor =
                new CompositeItemProcessor<>();
        try {
            File[] files = getFilesInPackage(packageName);
            List<String> chosenProcessorNames = filterFilesByProcessorClassNames(files, processorNames);
            List<ProcessorInterface> processors = instantiateProcessors(packageName, chosenProcessorNames);
            compositeProcessor.setDelegates(processors);
        } catch (IOException | ClassNotFoundException | InstantiationException | ClassCastException |
                InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("Can't initialize custom processors!");
            e.printStackTrace();
            return null;
        }

        return compositeProcessor;
    }

    private File[] getFilesInPackage(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        URL resource = resources.nextElement();
        File directory = new File(resource.getFile());
        return directory.listFiles();
    }

    private List<String> filterFilesByProcessorClassNames(File[] files, List<String> classNames) {
        List<String> acceptedClassNames = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileName = removeJavaFileExtension(file.getName());
                if (classNames.contains(fileName)) {
                    acceptedClassNames.add(fileName);
                }
            }
        }
        return acceptedClassNames;
    }

    private List<ProcessorInterface> instantiateProcessors(String packageName, List<String> classNames)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, ClassCastException {
        List<ProcessorInterface> instances = new ArrayList<>();
        for (String className : classNames) {
            Class<?> processorClass = Class.forName(packageName + '.' + className);
            Object processor = processorClass.getConstructor().newInstance();
            instances.add((ProcessorInterface)processor);
        }
        return instances;
    }

    private String removeJavaFileExtension(String fileName) {
        return fileName.substring(0, fileName.length() - 6); // no .class
    }
}
