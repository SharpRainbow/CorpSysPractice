package ru.mirea.game;

import ru.mirea.ClientActions;
import ru.mirea.Utils;
import ru.mirea.client.Client;
import ru.mirea.logger.CodeMasterLogRecord;
import ru.mirea.logger.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс, содержащий логику управления игрой, в которой участвует несколько клиентов
 */
public class GameSessionManager {

    private final String BROADCAST_MESSAGE_PROPERTY = "broadcastMessage";
    private final Logger logger;
    private final CodeMasterGame game;
    private String broadcastMessage;
    private CodeMasterLogRecord logRecord;
    private final PropertyChangeSupport propertyChangeSupport;
    private final Set<Client> playersReady = ConcurrentHashMap.newKeySet();
    private final AtomicInteger connectedPlayers = new AtomicInteger(0);

    public GameSessionManager(Logger logger, GameConfig gameConfig) {
        this.logger = logger;
        game = new CodeMasterGame(new CodeMasterGame.OnTimerEndListener() {
            @Override
            public void onTimerEnd() {
                finishGame(null);
            }
        }, gameConfig);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public boolean playerConnected() {
        if (connectedPlayers.get() == game.config().maxPlayers())
            return false;
        connectedPlayers.incrementAndGet();
        sendBroadcastMessage("Подключено игроков: " + connectedPlayers.get());
        return true;
    }

    public void playerDisconnected(Client client) {
        connectedPlayers.decrementAndGet();
        playersReady.remove(client);
        sendBroadcastMessage("Подключено игроков: " + connectedPlayers.get());
        if (
                game.isGameRunning() &&
                        connectedPlayers.get() < game.config().minPlayers()
        ) {
            gameFinishCall();
            sendBroadcastMessage("Для продолжения игры недостаточно игроков! игра отменена!");
        } else if (
                !game.isGameRunning() &&
                        playersReady.size() >= game.config().minPlayers() &&
                        playersReady.size() == connectedPlayers.get()
        ) {
            gameStartCall();
            sendBroadcastMessage("Игра начата! Отгадайте код!");
        }
    }

    public void processClientMessage(Client client, String message) {
        if (game.isGameRunning()) {
            GuessResult result = gameGuessCall(client.toString(), message);
            if (result.getbMarkers() == 4) {
                client.sendMessage("Вы победили!");
                finishGame(client);
            } else {
                client.sendMessage(result.toString());
            }
        } else if (connectedPlayers.get() < game.config().minPlayers()) {
            client.sendMessage(
                    String.format(
                            "Ожидание игроков: %d/%d",
                            connectedPlayers.get(),
                            game.config().minPlayers()
                    )
            );
        } else if (playersReady.size() < connectedPlayers.get()) {
            if (message.equals(ClientActions.READY.getName())) {
                playersReady.add(client);
            }
            client.sendMessage(
                    String.format(
                            "Ожидание готовности игроков: %d/%d",
                            playersReady.size(),
                            connectedPlayers.get()
                    )
            );
            if (playersReady.size() == connectedPlayers.get()) {
                gameStartCall();
                sendBroadcastMessage("Игра начата! Отгадайте код!");
            }
        }
    }

    public void endSession() {
        game.quit();
    }

    public int getConnectedPlayers() {
        return connectedPlayers.get();
    }

    public boolean isGameRunning() {
        return game.isGameRunning();
    }

    public void subscribeBroadcastMessage(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(BROADCAST_MESSAGE_PROPERTY, listener);
    }

    public void unsubscribeBroadcastMessage(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(BROADCAST_MESSAGE_PROPERTY, listener);
    }

    private void finishGame(Client winner) {
        if (winner == null) {
            sendBroadcastMessage("Время вышло!");
        } else {
            sendBroadcastMessage("Победитель найден: " + winner.toString());
            logRecord.setWinner(winner.toString());
        }
        gameFinishCall();
        sendBroadcastMessage("Загаданный код: " + game.revealAndResetCode());
        playersReady.clear();
    }

    private void sendBroadcastMessage(String message) {
        propertyChangeSupport.firePropertyChange(BROADCAST_MESSAGE_PROPERTY, broadcastMessage, message);
        broadcastMessage = message;
    }

    private void gameStartCall() {
        logRecord = new CodeMasterLogRecord();
        logRecord.setStartTimestamp(Utils.getTimeStamp());
        String secretCode = game.startGame();
        logRecord.setSecretCode(secretCode);
    }

    private void gameFinishCall() {
        logRecord.setEndTimestamp(Utils.getTimeStamp());
        game.finishGame();
        logger.save(logRecord);
    }

    private GuessResult gameGuessCall(String client, String message) {
        GuessResult result = game.checkGuess(message);
        logRecord.addTry(client, message, result);
        return result;
    }

}
