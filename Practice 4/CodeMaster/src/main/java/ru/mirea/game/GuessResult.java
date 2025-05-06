package ru.mirea.game;

/**
 * Класс для хранения результата попытки отгадать код
 */
public class GuessResult {

    private int bMarkers;
    private int wMarkers;
    private String errorMessage;

    public GuessResult(int bMarkers, int wMarkers) {
        this.bMarkers = bMarkers;
        this.wMarkers = wMarkers;
    }

    public GuessResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getbMarkers() {
        return bMarkers;
    }

    public int getwMarkers() {
        return wMarkers;
    }

    @Override
    public String toString() {
        if (errorMessage == null)
            return String.format("Черные маркеры=%d, Белые маркеры=%d", bMarkers, wMarkers);
        else
            return errorMessage;
    }
}
