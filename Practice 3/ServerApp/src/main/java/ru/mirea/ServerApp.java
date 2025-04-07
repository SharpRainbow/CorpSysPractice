package ru.mirea;

import java.util.Scanner;

/**
 * Класс, содержащий логику управления программой из консоли.
 */
public class ServerApp {

    private Server server;
    private String message = "Write exit to stop server!";
    private boolean stopped = false;

    public ServerApp() {
        server = new Server();
        server.setOnErrorOccurredListener(new Server.OnErrorOccurredListener() {
            @Override
            public void onError(Exception e) {
                System.err.println("Exception occurred: ");
                e.printStackTrace();
                server.stop();
                stopped = true;
            }
        });
        server.setOnServerMessageListener(new Server.OnServerMessageListener() {
            @Override
            public void onServerMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void run() {
        System.out.println(message);
        server.start();
        Scanner scanner = new Scanner(System.in);
        while (!stopped) {
            if (scanner.nextLine().equals("exit")) {
                server.stop();
                stopped = true;
            }
        }
    }

}
