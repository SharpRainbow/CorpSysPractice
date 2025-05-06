package ru.mirea.client;

public interface Client {

    void connect() throws Exception;

    void disconnect();

    void sendMessage(String message);

    boolean isConnected();

}
