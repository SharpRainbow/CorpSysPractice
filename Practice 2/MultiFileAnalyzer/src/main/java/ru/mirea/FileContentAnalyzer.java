package ru.mirea;

/**
 * Класс, позволяющий анализировать файлы.
 */
public class FileContentAnalyzer implements ContentAnalyzer<String, FileAnalysis> {

    private String fileName;
    private int wordCount = 0;
    private int symbolCount = 0;

    @Override
    public void prepare(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public FileAnalysis generateResult() throws IllegalStateException {
        if (fileName == null)
            throw new IllegalStateException("No file info provided!");
        return new FileAnalysis(fileName, wordCount, symbolCount);
    }

    @Override
    public void analyzePart(String line) throws IllegalStateException {
        if (fileName == null)
            throw new IllegalStateException("No file info provided!");
        line = line.trim();
        if (line.isEmpty())
            return;
        wordCount += line.split("\\s+").length;
        symbolCount += line.toCharArray().length;
    }

}
