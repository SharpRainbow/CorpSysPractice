package ru.mirea;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;


/**
 * Класс задачи для анализа файла.
 */
public class FileTask implements Runnable {

    private final String fileName;
    private FileAnalysis taskResult;
    private final ContentAnalyzer<String, FileAnalysis> analyzer;
    private OnTaskCompleteListener onTaskCompleteListener;
    private final DataRepository repository;

    public FileTask(String fileName, DataRepository repository, ContentAnalyzer<String, FileAnalysis> analyzer) {
        this.repository = repository;
        this.fileName = fileName;
        this.analyzer = analyzer;
    }

    /**
     * Данный метод позволяет установить слущатель завершения задачи.
     * @param onTaskCompleteListener Реализация интерфейса слушателя.
     */
    public void setOnTaskCompleteListener(OnTaskCompleteListener onTaskCompleteListener) {
        this.onTaskCompleteListener = onTaskCompleteListener;
    }

    @Override
    public void run() {
        try {
            analyzer.prepare(Utils.getFileName(fileName));
            try (BufferedReader rd = repository.getReader(fileName)) {
                String line = rd.readLine();
                while (line != null) {
                    analyzer.analyzePart(line);
                    line = rd.readLine();
                }
                taskResult = analyzer.generateResult();
            }
        } catch (InvalidPathException | FileNotFoundException e) {
            taskResult = new FileAnalysis("Файл не найден!");
        } catch (IOException e) {
            taskResult = new FileAnalysis("Не удалось открыть файл!");
        }
        if (onTaskCompleteListener != null)
            onTaskCompleteListener.onTaskComplete(taskResult);
    }

    interface OnTaskCompleteListener {
        void onTaskComplete(FileAnalysis analysis);
    }

}
