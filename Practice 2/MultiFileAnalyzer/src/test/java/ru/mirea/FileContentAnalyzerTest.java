package ru.mirea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileContentAnalyzerTest {

    private FileContentAnalyzer fileContentAnalyzer;
    private String fileName = "test.txt";
    private String sampleText = "This is a sample text.";

    @BeforeEach
    void setUp() {
        fileContentAnalyzer = new FileContentAnalyzer();
    }

    @Test
    void whenDataPassed_thenResultIsCorrect() {
        fileContentAnalyzer.prepare(fileName);
        fileContentAnalyzer.analyzePart(sampleText);
        FileAnalysis result = fileContentAnalyzer.generateResult();
        assertEquals(result.getSymbolCount(), 22);
        assertEquals(result.getWordCount(), 5);
    }

    @Test
    void whenMultipleDataPassed_thenResultIsCorrect() {
        fileContentAnalyzer.prepare(fileName);
        fileContentAnalyzer.analyzePart(sampleText);
        fileContentAnalyzer.analyzePart(sampleText);
        FileAnalysis result = fileContentAnalyzer.generateResult();
        assertEquals(result.getSymbolCount(), 44);
        assertEquals(result.getWordCount(), 10);
    }

    @Test
    void whenPrepareIsNotCalled_thenExceptionIsThrown() {
        assertThrows(IllegalStateException.class, () -> fileContentAnalyzer.analyzePart(sampleText));
    }

}