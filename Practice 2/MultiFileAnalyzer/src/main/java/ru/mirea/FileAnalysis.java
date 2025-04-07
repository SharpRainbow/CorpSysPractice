package ru.mirea;

/**
 * Класс, содержащий результаты анализа данных.
 */
public class FileAnalysis{

    private String name;
    private int wordCount;
    private int symbolCount;
    private String errorMessage;

    public FileAnalysis(String name,
                        int wordCount,
                        int symbolCount) {
        this.name = name;
        this.wordCount = wordCount;
        this.symbolCount = symbolCount;
    }

    public FileAnalysis(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getSymbolCount() {
        return symbolCount;
    }

    @Override
    public String toString() {
        if (errorMessage == null) {
            return String.format("%s: %d слов, %d символов", name, wordCount, symbolCount);
        } else {
            return errorMessage;
        }
    }
}