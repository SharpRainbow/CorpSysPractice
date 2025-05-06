package ru.mirea.server;

public interface Server {

    String SERVER_ADDRESS = "0.0.0.0";

    void start();

    void stop();

}
