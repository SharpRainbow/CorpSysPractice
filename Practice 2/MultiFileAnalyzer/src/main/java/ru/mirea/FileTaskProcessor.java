package ru.mirea;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс для запуска и управления задачами анализа файлов.
 */
public class FileTaskProcessor implements TaskProcessor<String> {

    private final ExecutorService executor;
    private final FileTaskFactory taskFactory;
    private final Phaser phaser;
    private final AtomicInteger wordCounter = new AtomicInteger(0);
    private final AtomicInteger charCounter = new AtomicInteger(0);

    public FileTaskProcessor(ExecutorService executor, FileTaskFactory taskFactory) {
        this.executor = executor;
        this.taskFactory = taskFactory;
        phaser = new Phaser(1);
    }

    private void submitTask(String task) {
        FileTask fileTask = taskFactory.createTask(task);
        fileTask.setOnTaskCompleteListener(new FileTask.OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(FileAnalysis analysis) {
                try {
                    TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(10, 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(analysis.toString());
                wordCounter.getAndAdd(analysis.getWordCount());
                charCounter.getAndAdd(analysis.getSymbolCount());
                phaser.arriveAndDeregister();
            }
        });
        phaser.register();
        executor.submit(fileTask);
    }

    @Override
    public void process(String task) {
        wordCounter.set(0);
        charCounter.set(0);
        submitTask(task);
        phaser.arriveAndAwaitAdvance();
        System.out.printf("итог: %d слов, %d символов.\n", wordCounter.get(), charCounter.get());
    }

    @Override
    public void process(Iterable<String> tasks) {
        wordCounter.set(0);
        charCounter.set(0);
        for (String task : tasks) {
            submitTask(task);
        }
        phaser.arriveAndAwaitAdvance();
        System.out.printf("итог: %d слов, %d символов.\n", wordCounter.get(), charCounter.get());
    }

    public void finish() {
        executor.shutdown();
    }
}
