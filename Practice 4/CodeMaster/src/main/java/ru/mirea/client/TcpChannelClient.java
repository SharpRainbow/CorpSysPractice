package ru.mirea.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Вариант клиента, взаимодействующим с байтовым каналом для отправки и получения данных
 */
public class TcpChannelClient implements Client {

    private final UUID uniqueId = UUID.randomUUID();
    protected final String ipAddress;
    protected final ByteBuffer bufferIn;
    protected final ByteBuffer bufferOut;
    protected final ByteChannel dataChannel;
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private ReadWriteListener readWriteListener;

    public TcpChannelClient(
            String ipAddress,
            ByteChannel dataChannel
    ) {
        this.ipAddress = ipAddress;
        this.dataChannel = dataChannel;
        this.bufferIn = ByteBuffer.allocate(2048);
        this.bufferOut = ByteBuffer.allocate(2048);
    }
    
    @Override
    public void setReadWriteListener(ReadWriteListener readWriteListener) {
        this.readWriteListener = readWriteListener;
    }

    @Override
    public void sendMessage(String message) {
        messageQueue.add(message);
    }

    @Override
    public int handleRead() throws IOException {
        int bytesIn = dataChannel.read(bufferIn);
        if (bytesIn < 0) {
            throw new IOException("Socket closed");
        }
        if (bytesIn > 0) {
            bufferIn.flip();
            if (readWriteListener != null) {
                readWriteListener.onDataRead(bufferIn);
            }
            bufferIn.clear();
        }
        return bytesIn;
    }

    @Override
    public int handleWrite() throws IOException {
        if (messageQueue.isEmpty())
            return 0;
        int bytesOut = 0;
        while (!messageQueue.isEmpty()) {
            bufferOut.put((messageQueue.poll() + "\n\r").getBytes());
            bufferOut.flip();
            bytesOut = dataChannel.write(bufferOut);
            bufferOut.compact();
        }
        if (readWriteListener != null) {
            readWriteListener.onDataWrite(bufferOut);
        }
        return bytesOut;
    }

    @Override
    public void disconnect() throws IOException {
        dataChannel.close();
    }

    @Override
    public String toString() {
        return String.format("Данные игрока: ipAddress=%s, uniqueId=%s", ipAddress, uniqueId);
    }
}
