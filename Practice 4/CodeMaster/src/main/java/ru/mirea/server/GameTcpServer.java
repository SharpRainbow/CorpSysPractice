package ru.mirea.server;

import ru.mirea.client.Client;
import ru.mirea.client.TcpChannelClientFactory;
import ru.mirea.game.GameConfig;
import ru.mirea.game.GameSessionManager;
import ru.mirea.logger.Logger;
import ru.mirea.logger.XmlLogger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

/**
 * Сервер, использущий менеджер игровых сессий для организации сетевой игры
 */
public class GameTcpServer extends AbstractTcpChannelServer {

    private final GameSessionManager sessionManager;
    private final PropertyChangeListener messageListener;

    public GameTcpServer(int port, Logger logger) {
        super(port, new TcpChannelClientFactory());
        sessionManager = new GameSessionManager(
                new XmlLogger(),
                new GameConfig(2, 4, 4, 60)
        );
        messageListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String message = (String) evt.getNewValue();
                sendBroadcastMessage(message);
            }
        };
        sessionManager.subscribeBroadcastMessage(messageListener);
    }

    @Override
    public void stop() {
        super.stop();
        sessionManager.endSession();
        sessionManager.unsubscribeBroadcastMessage(messageListener);
    }

    @Override
    protected void onError(Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
    }

    @Override
    protected void onServerMessage(String message) {
        System.out.println("Событие сервера: " + message);
    }

    @Override
    protected void onClientConnected(SelectionKey clientKey) throws IOException {
        Client client = (Client) clientKey.attachment();
        if (!sessionManager.playerConnected()) {
            client.disconnect();
            clientKey.cancel();
            throw new IOException("Too many players!");
        }
        client.setReadWriteListener(new Client.ReadWriteListener() {
            @Override
            public void onDataRead(ByteBuffer buffer) {
                byte[] arr = new byte[buffer.remaining()];
                buffer.get(arr);
                String message = new String(arr, StandardCharsets.UTF_8).trim();
                sessionManager.processClientMessage(client, message);
                clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
            }

            @Override
            public void onDataWrite(ByteBuffer buffer) {
                clientKey.interestOps(SelectionKey.OP_READ);
            }
        });
    }

    @Override
    protected void onClientDisconnected(SelectionKey clientKey) {
        Client client = (Client) clientKey.attachment();
        onServerMessage("Игрок отключился " + client.toString());
        sessionManager.playerDisconnected(client);
    }

}
