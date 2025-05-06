package ru.mirea.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Абстрактный клиент, позволяющий подключится к серверу по TCP
 * и обмениваться с ним текстовыми сообщениями
 */
public abstract class AbstractTcpClient implements Client {

    private AsynchronousSocketChannel socketChannel;
    private final InetSocketAddress serverAddress;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final Queue<String> messageQueue;
    private final AtomicBoolean writeInProgress = new AtomicBoolean(false);
    private boolean isConnected = false;

    public AbstractTcpClient(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
        this.readBuffer = ByteBuffer.allocate(1024);
        this.writeBuffer = ByteBuffer.allocate(1024);
        this.messageQueue = new ConcurrentLinkedQueue<String>();
    }

    public void connect() throws Exception {
        socketChannel = AsynchronousSocketChannel.open();
        Future<Void> future = socketChannel.connect(serverAddress);
        future.get(15, TimeUnit.SECONDS);
        isConnected = true;
        doRead();
    }

    private void doRead() {
        socketChannel.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                if (result == -1) {
                    onErrorOccurred(new Exception("Не удалось прочитать сообщение"));
                    return;
                }
                readBuffer.flip();
                String msg = Charset.defaultCharset().decode(readBuffer).toString();
                onMessageReceived(msg);
                readBuffer.clear();
                doRead();
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                onErrorOccurred(exc);
            }
        });
    }

    private void doWrite() {
        String msg = messageQueue.poll();
        if (msg == null || !isConnected) {
            writeInProgress.set(false);
            return;
        }
        writeBuffer.clear();
        writeBuffer.put(msg.getBytes());
        writeBuffer.flip();
        socketChannel.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {

            @Override
            public void completed(Integer result, Void attachment) {
                if (writeBuffer.hasRemaining()) {
                    socketChannel.write(writeBuffer, null, this);
                } else {
                    doWrite();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                writeInProgress.set(false);
                onErrorOccurred(exc);
            }
        });
    }

    @Override
    public void sendMessage(String msg) {
        messageQueue.add(msg);
        if (writeInProgress.compareAndSet(false, true)) {
            doWrite();
        }
    }

    @Override
    public void disconnect() {
        if (isConnected && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException ignored) {}
            isConnected = false;
            onDisconnected();
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    abstract void onMessageReceived(String message);

    abstract void onErrorOccurred(Throwable ex);

    abstract void onDisconnected();

}
