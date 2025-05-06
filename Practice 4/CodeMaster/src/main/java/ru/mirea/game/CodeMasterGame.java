package ru.mirea.game;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Класс содержащий логику игры - начало, окончание, создание кода и проверка ответов
 */
public class CodeMasterGame {

    private GameConfig gameConfig;
    private Random rand = new Random();
    private String code;
    private OnTimerEndListener onTimerEndListener;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> gameEndTask;

    public CodeMasterGame(
            OnTimerEndListener onTimerEndListener,
            GameConfig gameConfig

    ) {
        this.gameConfig = gameConfig;
        this.onTimerEndListener = onTimerEndListener;
        genCode();
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public String startGame() {
        System.out.println("Secret code is " + code);
        gameEndTask = executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (onTimerEndListener != null && !Thread.currentThread().isInterrupted()) {
                    onTimerEndListener.onTimerEnd();
                }
                gameEndTask = null;
            }
        }, gameConfig.roundDuration(), TimeUnit.SECONDS);
        return code;
    }

    public boolean isGameRunning() {
        return gameEndTask != null;
    }

    public void finishGame() {
        if (gameEndTask != null) {
            gameEndTask.cancel(true);
            gameEndTask = null;
        }
    }

    public void quit() {
        finishGame();
        executorService.shutdown();
    }

    public void genCode() {
        if (gameConfig.codeSymbolCount() <= 0) {
            gameConfig = new GameConfig(
                    gameConfig.minPlayers(),
                    gameConfig.maxPlayers(),
                    4,
                    gameConfig.roundDuration()
            );
            code = "1234";
        } else {
            code = String.valueOf(Math.abs(rand.nextInt())).substring(0, gameConfig.codeSymbolCount());
        }
    }

    public String revealAndResetCode() {
        String answer = code;
        genCode();
        return answer;
    }

    public GameConfig config() {
        return gameConfig;
    }

    public GuessResult checkGuess(String guess) {
        if (!isGameRunning())
            return new GuessResult("Игра не начата! Ответы не принимаются!");
        if (guess.isEmpty() || guess.length() != gameConfig.codeSymbolCount()) {
            return new GuessResult(
                    String.format(
                            "Неверные входные данные! Код должен содержать %d символов!",
                            gameConfig.codeSymbolCount()
                    )
            );
        }
        int wMarks = 0;
        int bMarks = 0;
        LinkedList<Character> codeUncheckedSymbols = new LinkedList<>();
        LinkedList<Character> guessUncheckedSymbols = new LinkedList<>();
        for (int i = 0; i < code.length(); i++) {
            if (guess.charAt(i) == code.charAt(i)) {
                bMarks++;
            } else {
                codeUncheckedSymbols.add(code.charAt(i));
                guessUncheckedSymbols.add(guess.charAt(i));
            }
        }
        for (char c : codeUncheckedSymbols) {
            if (guessUncheckedSymbols.contains(c)) {
                wMarks++;
                guessUncheckedSymbols.remove((Character) c);
            }
        }
        return new GuessResult(bMarks, wMarks);
    }

    public interface OnTimerEndListener {
        void onTimerEnd();
    }

}
