package ru.mirea;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientApp {

    private Client client;
    private String exitHint = "Write exit to return to menu";
    private String menu = "Select an option to proceed:\n1.Connect\n2.Exit";
    private MyScanner scanner;
    private CountDownLatch latch;

    public ClientApp() {
        scanner = new MyScanner();
        client = new Client();
        client.setOnErrorOccurredListener(new Client.OnErrorOccurredListener() {
            @Override
            public void onErrorOccurred(Exception e) {
                String message = e.getMessage();
                if (message == null) {
                    message = "Unknown exception. Check stack trace";
                    e.printStackTrace();
                }
                System.err.printf("Exception occured: %s\n", message);
                client.stop();
                latch.countDown();
            }
        });
        client.setOnMessageReceivedListener(new Client.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) {
                System.out.println(message);
            }
        });
        client.setOnClientConnectedListener(new Client.OnClientConnectedListener() {
            @Override
            public void onClientConnected() {
                latch.countDown();
            }
        });
    }

    public void run() {
        while (true) {
            System.out.println(menu);
            System.out.print("[Selected option]: ");
            String input = scanner.nextLine();
            latch = new CountDownLatch(1);
            try {
                if (input.equals("1")) {
                    client.start();
                    System.out.println("Connecting to server...");
                    latch.await();
                    if (client.isRunning()) {
                        System.out.println(exitHint);
                        System.out.print("[Enter your message]: ");
                        while (client.isRunning()) {
                            if (scanner.hasNextLine()) {
                                input = scanner.nextLine();
                                if (input.equals("exit")) {
                                    client.stop();
                                    break;
                                }
                                client.sendFile(input);
                                System.out.print("[Enter your message]: ");
                            } else
                                TimeUnit.MILLISECONDS.sleep(10);
                        }
                    }
                } else if (input.equals("2")) {
                    client.stop();
                    break;
                }
            } catch (InterruptedException e) {
                System.out.println("Program interrupted!");
            }
        }
    }

}
