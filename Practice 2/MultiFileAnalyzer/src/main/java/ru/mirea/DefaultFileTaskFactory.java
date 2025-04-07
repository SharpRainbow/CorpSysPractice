package ru.mirea;

import java.util.function.Supplier;

/**
 * Фабрика для создания объектов Task, позволяющих анализировать файлы.
 */
public class DefaultFileTaskFactory implements FileTaskFactory {

    private final DataRepository repository;
    private final Supplier<ContentAnalyzer<String, FileAnalysis>> analyzerSupplier;

    public DefaultFileTaskFactory(
            DataRepository repository,
            Supplier<ContentAnalyzer<String, FileAnalysis>> analyzerSupplier
    ) {
        this.repository = repository;
        this.analyzerSupplier = analyzerSupplier;
    }

    @Override
    public FileTask createTask(String fileName) {
        return new FileTask(fileName, repository, analyzerSupplier.get());
    }
}
