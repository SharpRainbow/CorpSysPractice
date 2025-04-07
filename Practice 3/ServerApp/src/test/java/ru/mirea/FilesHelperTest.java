package ru.mirea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesHelperTest {

    private static final String TEST_MESSAGE = "Hello World!";
    private static final String TEST_DIR_NAME = "test";
    private static final String TEST_FILE_NAME = "test.txt";
    private static String analysisResult;

    @BeforeAll
    static void setUp() {
        analysisResult = String.format(
                "Filename: %s\nLines: %d, Words: %d, Symbols: %d\n",
                TEST_FILE_NAME,
                TEST_MESSAGE.split("\n").length,
                TEST_MESSAGE.split(" ").length,
                TEST_MESSAGE.toCharArray().length
        );
    }

    @Test
    void whenFileCreationCalled_thenFileExists() {
        Path p = Path.of(TEST_DIR_NAME, TEST_FILE_NAME);
        FilesHelper.writeSimpleFile(p, TEST_MESSAGE);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(Files.exists(p));
    }

    @Test
    void whenFileCreationCalled_thenFileContainsProvidedData() {
        Path p = Path.of(TEST_DIR_NAME, TEST_FILE_NAME);
        FilesHelper.writeSimpleFile(p, TEST_MESSAGE);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = Files.newBufferedReader(p)) {
            System.out.println("aaa");
            assertEquals(TEST_MESSAGE, reader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenExistingPathProvided_thenAnalysisResultAreCorrect() {
        Path p = Path.of(TEST_DIR_NAME, TEST_FILE_NAME);
        FilesHelper.writeSimpleFile(p, TEST_MESSAGE);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(analysisResult, FilesHelper.analyze(p));
    }

    @AfterEach
    void tearDown() {
        Path p = Path.of(TEST_DIR_NAME);
        try {
            if (Files.exists(p)) {
                try (Stream<Path> paths = Files.walk(p)) {
                    paths.map(Path::toFile).forEach(File::delete);
                }
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}