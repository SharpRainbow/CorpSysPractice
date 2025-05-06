package ru.mirea.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CodeMasterGameTest {

    private CodeMasterGame codeMasterGame;

    @Test
    void whenTimeIsUp_thenGameIsNotRunning() {
        codeMasterGame = new CodeMasterGame(
                null,
                new GameConfig(2, 2, 2, 1)
        );
        codeMasterGame.startGame();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(codeMasterGame.isGameRunning());
    }

    @Test
    void whenGameIsFinished_thenGameEndListenerIsNotCalled() {
        AtomicBoolean gameIsFinished = new AtomicBoolean(false);
        codeMasterGame = new CodeMasterGame(
                new CodeMasterGame.OnTimerEndListener() {
                    @Override
                    public void onTimerEnd() {
                        gameIsFinished.set(true);
                    }
                },
                new GameConfig(2, 2, 2, 1)
        );
        codeMasterGame.finishGame();
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(gameIsFinished.get());
    }

    @Test
    void whenGuessIsChecked_thenResultIsCorrect() {
        codeMasterGame = new CodeMasterGame(
                null,
                new GameConfig(2, 2, 0, 1)
        );
        char[] code = codeMasterGame.startGame().toCharArray();
        char tmp = code[0];
        code[0] = code[1];
        code[1] = tmp;
        GuessResult result = codeMasterGame.checkGuess(new String(code));
        assertEquals(2, result.getbMarkers());
        assertEquals(2, result.getwMarkers());
    }

    @Test
    void whenGuessIsCorrect_thenResultIsCorrect() {
        codeMasterGame = new CodeMasterGame(
                null,
                new GameConfig(2, 2, 2, 1)
        );
        String code = codeMasterGame.startGame();
        GuessResult result = codeMasterGame.checkGuess(code);
        assertEquals(codeMasterGame.config().codeSymbolCount(), result.getbMarkers());
    }

    @Test
    void whenGameIsNotRunning_thenGuessReturnsError() {
        codeMasterGame = new CodeMasterGame(
                null,
                new GameConfig(2, 2, 2, 1)
        );
        String code = codeMasterGame.startGame();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(codeMasterGame.isGameRunning());
        GuessResult result = codeMasterGame.checkGuess(code);
        assertEquals(0, result.getbMarkers());
        assertEquals(0, result.getwMarkers());
    }

    @AfterEach
    void tearDown() {
        if (codeMasterGame != null) {
            codeMasterGame.quit();
        }
    }

}