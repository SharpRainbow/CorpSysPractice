package ru.mirea.app;

import ru.mirea.client.Client;
import ru.mirea.client.GameTcpClient;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Управляющий класс приложение. Принимает ввод пользователя и отправляет сообщения
 */
public class ClientApp {

    private final Scanner scanner;

    public ClientApp() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Введите exit для выхода");
        while (true) {
            InetSocketAddress address = promptForAddress();
            if (address == null) break;

            Client client = new GameTcpClient(address);
            if (!tryConnect(client)) continue;

            handleServerDialogue(client);
        }
    }
    private InetSocketAddress promptForAddress() {
        while (true) {
            System.out.println("Введите адрес подключения в формате IP:PORT");
            String command = scanner.nextLine();
            if (command.equalsIgnoreCase("exit")) return null;

            String[] parts = command.split(":");
            if (parts.length != 2) {
                System.out.println("Неверный формат адреса");
                continue;
            }
            try {
                int port = Integer.parseInt(parts[1]);
                return new InetSocketAddress(parts[0], port);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат порта");
            }
        }
    }

    private boolean tryConnect(Client client) {
        System.out.print("Подключение...");
        try {
            client.connect();
            System.out.println("OK");
            return true;
        } catch (Exception e) {
            System.out.println("\nНе удалось подключиться: " + e.getMessage());
            return false;
        }
    }

    /**
     * Метод, управляющий диалогом с сервером
     * @param client Клиент, поддерживающий сетевое соединение с сервером
     */
    private void handleServerDialogue(Client client) {
        System.out.println("Введите exit для отсоединения");
        while (client.isConnected()) {
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) {
                client.disconnect();
                break;
            }
            client.sendMessage(message);
        }
    }
}
