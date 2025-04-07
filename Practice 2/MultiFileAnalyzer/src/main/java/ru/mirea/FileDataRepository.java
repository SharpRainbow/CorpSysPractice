package ru.mirea;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Класс для получения данных из файла.
 */
public enum FileDataRepository implements DataRepository {

    INSTANCE;

    @Override
    public InputStream getStream(String fileName) throws IOException {
        Path p = Paths.get(fileName);
        return new BufferedInputStream(Files.newInputStream(p, StandardOpenOption.READ));
    }

    @Override
    public BufferedReader getReader(String fileName) throws IOException {
        Path p = Paths.get(fileName);
        return Files.newBufferedReader(p);
    }

}
