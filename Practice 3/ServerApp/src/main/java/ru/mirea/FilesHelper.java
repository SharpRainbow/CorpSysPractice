package ru.mirea;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FilesHelper {

    private static final String ERROR_MSG = "Unable to analyze file!";

    public static String analyze(Path file) {
        String result = ERROR_MSG;
        if (Files.exists(file))
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                AtomicInteger lineCount = new AtomicInteger();
                AtomicInteger wordCount = new AtomicInteger();
                AtomicLong symbolCount = new AtomicLong();
                reader.lines().filter(string -> !string.isEmpty()).forEach(line -> {
                    lineCount.getAndIncrement();
                    wordCount.addAndGet(line.split(" ").length);
                    symbolCount.addAndGet(line.chars().count());
                });
                String fileName = file.getFileName().toString();
                fileName = fileName.substring(fileName.indexOf('-') + 1);
                result = String.format(
                        "Filename: %s\nLines: %d, Words: %d, Symbols: %d\n",
                        fileName,
                        lineCount.get(),
                        wordCount.get(),
                        symbolCount.get()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        return result;
    }

    public static void writeSimpleFile(Path path, String content) {
        if (Files.exists(path))
            return;
        try {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
