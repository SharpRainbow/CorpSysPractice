package ru.mirea;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Utils {

    static String getFileName(String filePath) throws InvalidPathException {
        return Paths.get(filePath).getFileName().toString();
    }

}
