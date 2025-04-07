package ru.mirea;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * Управляющий класс для запуска приложения.
 */
public class AnalyzerApp {

    private final FileTaskProcessor taskProcessor;
    private final Scanner scanner;

    public AnalyzerApp() {
        scanner = new Scanner(System.in);
        taskProcessor = new FileTaskProcessor(
                Executors.newFixedThreadPool(5),
                new DefaultFileTaskFactory(
                        FileDataRepository.INSTANCE,
                        FileContentAnalyzer::new
                )
        );
    }

    /**
     * Запуск цикла управления приложением.
     */
    public void run() {
        while (true) {
            System.out.print("Введите количество файлов для анализа: ");
            int fileCount = scanner.nextInt();
            scanner.skip("\n");
            if (fileCount <= 0) {
                System.out.println("Выход...");
                break;
            }
            ArrayList<String> paths = new ArrayList<>();
            while (fileCount > 0) {
                System.out.print("Путь к файлу: ");
                String path = scanner.nextLine();
                paths.add(path);
                fileCount--;
            }
            taskProcessor.process(paths);
        }
        taskProcessor.finish();
    }

}
