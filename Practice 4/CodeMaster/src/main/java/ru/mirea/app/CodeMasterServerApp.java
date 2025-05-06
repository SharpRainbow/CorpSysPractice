package ru.mirea.app;

import ru.mirea.logger.XmlLogger;
import ru.mirea.server.GameTcpServer;
import ru.mirea.server.Server;

import java.util.Scanner;

/**
 * Класс запускающий приложение
 */
public class CodeMasterServerApp {

    private final static int PORT = 5555;
    private final Scanner scanner;
    private final Server server;

    public CodeMasterServerApp() {
        scanner = new Scanner(System.in);
        server = new GameTcpServer(PORT, new XmlLogger());
    }

    public void run() {
        server.start();
        System.out.println("Введите exit для выхода...");
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("exit")) {
                break;
            }
        }
        server.stop();
    }

}
