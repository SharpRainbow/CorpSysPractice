package ru.mirea;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileTaskTest {

    private static ExecutorService executorService;
    private String fileName = "test.txt";

    @BeforeAll
    static void setUp() {
        executorService = Executors.newFixedThreadPool(5);
    }

    @Test
    void whenTaskIsSubmitted_thenResultIsReceived() {
        FileTask task = new FileTask(
                fileName,
                MockDataRepository.INSTANCE,
                new FileContentAnalyzer()
        );
        AtomicReference<FileAnalysis> fileAnalysis = new AtomicReference<>();
        task.setOnTaskCompleteListener(new FileTask.OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(FileAnalysis analysis) {
                fileAnalysis.set(analysis);
            }
        });
        Future<?> job = executorService.submit(task);
        int wordsCount = MockDataRepository.INSTANCE.getMockData().split("\\s+").length;
        int charsCount = MockDataRepository.INSTANCE.getMockData()
                .lines().map(String::toCharArray).map(Array::getLength).reduce(0, Integer::sum);
        try {
            job.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(fileAnalysis.get());
        assertEquals(wordsCount, fileAnalysis.get().getWordCount());
        assertEquals(charsCount, fileAnalysis.get().getSymbolCount());
    }

    @Test
    void whenUnknownFileSubmitted_thenResultIsError() throws UnsupportedEncodingException {
        FileTask task = new FileTask(
                fileName,
                FileDataRepository.INSTANCE,
                new FileContentAnalyzer()
        );
        AtomicReference<FileAnalysis> fileAnalysis = new AtomicReference<>();
        task.setOnTaskCompleteListener(new FileTask.OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(FileAnalysis analysis) {
                fileAnalysis.set(analysis);
            }
        });
        Future<?> job = executorService.submit(task);
        try {
            job.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(fileAnalysis.get());
        assertEquals("Не удалось открыть файл!", fileAnalysis.get().toString());
    }

    @AfterAll
    static void tearDown() {
        executorService.shutdown();
    }
}