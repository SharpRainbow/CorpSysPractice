package ru.mirea.client;

import java.nio.channels.ByteChannel;

public class TcpChannelClientFactory implements ClientFactory {

    @Override
    public Client createClient(String name, ByteChannel channel) {
        return new TcpChannelClient(name, channel);
    }

}
