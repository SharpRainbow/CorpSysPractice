package ru.mirea.client;

import java.net.InetSocketAddress;

/**
 * Реализация клиента с определенными действиями на получение сообщения,
 * возникновения ошибки и отключение от сервера
 */
public class GameTcpClient extends AbstractTcpClient {

    public GameTcpClient(
            InetSocketAddress serverAddress
    ) {
        super(serverAddress);
    }

    @Override
    void onMessageReceived(String message) {
        System.out.print(message);
    }

    @Override
    void onErrorOccurred(Throwable ex) {
        if (isConnected()) {
            disconnect();
            System.out.println("Ошибка соединения!");
        }
    }

    @Override
    void onDisconnected() {
        System.out.println("Выполнено отключение от сервера!");
    }

}
