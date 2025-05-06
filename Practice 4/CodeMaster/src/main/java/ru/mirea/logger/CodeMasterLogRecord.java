package ru.mirea.logger;

import ru.mirea.game.GuessResult;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Класс, представляющий запись в журнале об одной игре
 */
public class CodeMasterLogRecord {

    private String startTimestamp;
    private String endTimestamp;
    private String secretCode;
    private Queue<String> tries = new ConcurrentLinkedQueue<>();
    private String winner;

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String[] getTries() {
        return tries.toArray(new String[0]);
    }

    public void setTries(String[] tries) {
        this.tries.addAll(Arrays.asList(tries));
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void addTry(String player, String message, GuessResult guess) {
        tries.add(String.format("%s, %s, %s", player, message, guess.toString()));
    }

}
