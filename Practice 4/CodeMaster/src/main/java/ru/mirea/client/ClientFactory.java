package ru.mirea.client;

import java.nio.channels.ByteChannel;

public interface ClientFactory {

    Client createClient(String name, ByteChannel channel);

}
