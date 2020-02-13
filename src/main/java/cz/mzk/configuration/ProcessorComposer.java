package cz.mzk.configuration;

import cz.mzk.processor.SolrDocProcessor;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * @author Aleksei Ermak
 */

@Component
public class ProcessorComposer {

    @Autowired
    private MigrationToolConfiguration toolConfiguration;

    private final String packageName = "cz.mzk.processor";

    public CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> composite() {
        CompositeItemProcessor<List<SolrInputDocument>, List<SolrInputDocument>> compositeProcessor =
                new CompositeItemProcessor<>();
        try {
            File[] files = getFilesInPackage(packageName);
            List<String> processorNames = toolConfiguration.getProcessorClassNames();
            List<String> chosenProcessorNames = filterFilesByProcessorClassNames(files, processorNames);
            List<SolrDocProcessor> processors = instantiateProcessors(packageName, chosenProcessorNames);
            compositeProcessor.setDelegates(processors);
        } catch (IOException | ClassNotFoundException | InstantiationException |
                InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
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

    private List<SolrDocProcessor> instantiateProcessors(String packageName, List<String> classNames)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        List<SolrDocProcessor> instances = new ArrayList<>();
        for (String className : classNames) {
            Class<?> processorClass = Class.forName(packageName + '.' + className);
            Object processor = processorClass.getConstructor().newInstance();
            instances.add((SolrDocProcessor)processor);
        }
        return instances;
    }

    private String removeJavaFileExtension(String fileName) {
        return fileName.substring(0, fileName.length() - 6); // no .class
    }
}
