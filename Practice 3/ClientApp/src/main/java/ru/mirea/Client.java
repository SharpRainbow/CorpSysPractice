package ru.mirea;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private final Object lock = new Object();
    private ExecutorService executor;
    private final ByteBuffer outBuffer;
    private final ByteBuffer inBuffer;
    private final ByteBuffer fileContentBuffer;
    private OnMessageReceivedListener onMessageReceivedListener;
    private OnErrorOccurredListener onErrorOccurredListener;
    private OnClientConnectedListener onClientConnectedListener;
    private Selector selector;
    private final AtomicBoolean running;
    private SelectionKey selectionKey;
    private PacketWriter packetWriter;

    public boolean isRunning() {
        return running.get();
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;
    }

    public void setOnErrorOccurredListener(OnErrorOccurredListener onErrorOccurredListener) {
        this.onErrorOccurredListener = onErrorOccurredListener;
    }

    public void setOnClientConnectedListener(OnClientConnectedListener onClientConnectedListener) {
        this.onClientConnectedListener = onClientConnectedListener;
    }

    public Client() {
        outBuffer = ByteBuffer.allocate(2048);
        inBuffer = ByteBuffer.allocate(2048);
        fileContentBuffer = ByteBuffer.allocate(1024);
        running = new AtomicBoolean(false);
        packetWriter = new PacketWriter();
    }

    public void sendMessage(String message) {
        outBuffer.put(message.getBytes());
        if (selectionKey != null) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    public void sendFile(String fileName) {
        Path path = Path.of(fileName);
        if (!fileName.isEmpty() && Files.exists(path)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fileContentBuffer.clear();
                    try (FileChannel channel = FileChannel.open(path)) {
                        long size = Files.size(path);
                        int bytesRead = 0;
                        long timestamp = System.currentTimeMillis();
                        while (size > 0 && isRunning()) {
                            synchronized (lock) {
                                if (outBuffer.remaining() == outBuffer.capacity()) {
                                    bytesRead = channel.read(fileContentBuffer);
                                    if (bytesRead == -1) break;
                                    size -= bytesRead;
                                    byte[] slice = new byte[bytesRead];
                                    fileContentBuffer.flip();
                                    fileContentBuffer.get(slice, 0, bytesRead);
                                    fileContentBuffer.clear();
                                    Packet packet = new Packet(
                                            path.getFileName().toString(),
                                            timestamp,
                                            size,
                                            slice
                                    );
                                    packetWriter.writePacket(packet, outBuffer);
                                    if (selectionKey != null) {
                                        selectionKey.interestOps(
                                                selectionKey.interestOps() | SelectionKey.OP_WRITE
                                        );
                                        selector.wakeup();
                                    }
                                }
                                lock.wait();
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        if (onErrorOccurredListener != null)
                            onErrorOccurredListener.onErrorOccurred(e);
                        else
                            throw new RuntimeException(e);
                    }
                }
            });
        } else {
            Exception e = new FileNotFoundException("Specified file does not exist");
            if (onErrorOccurredListener != null)
                onErrorOccurredListener.onErrorOccurred(e);
            else
                throw new RuntimeException(e);
        }
    }

    public void start() {
        executor = Executors.newFixedThreadPool(2);
        packetWriter.setup();
        outBuffer.clear();
        inBuffer.clear();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try (SocketChannel socketChannel = SocketChannel.open()) {
                    selector = SelectorProvider.provider().openSelector();
                    socketChannel.configureBlocking(false);
                    selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
                    socketChannel.connect(new InetSocketAddress("127.0.0.1", 5555));
                    running.set(true);
                    message("Client started");
                    while (running.get()) {
                        selector.select();
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();

                            if (key.isConnectable()) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                if (channel.finishConnect()) {
                                    message("Connected to server!");
                                    key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
                                    if (onClientConnectedListener != null)
                                        onClientConnectedListener.onClientConnected();
                                    else
                                        message("Client connected");
                                }
                            }

                            if (key.isReadable()) {
                                readData(key);
                            }

                            if (key.isWritable()) {
                                writeData(key);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (onErrorOccurredListener != null)
                        onErrorOccurredListener.onErrorOccurred(e);
                    else
                        throw new RuntimeException(e);
                } finally {
                    running.set(false);
                    try {
                        if (selector != null) {
                            selector.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                selector = null;
                selectionKey = null;
                message("Client stopped");
            }
        });
    }

    private void message(String message) {
        if (onMessageReceivedListener != null) {
            onMessageReceivedListener.onMessageReceived(message);
        }
    }

    public void stop() {
        running.set(false);
        if (executor != null)
            executor.shutdown();
        if (selector != null)
            selector.wakeup();
    }

    private void writeData(SelectionKey key) throws IOException {
        synchronized (lock) {
            if (outBuffer.position() > 0) {
                outBuffer.flip();
                SocketChannel channel = (SocketChannel) key.channel();
                channel.write(outBuffer);
                outBuffer.compact();
                if (outBuffer.position() > 0) {
                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                } else {
                    key.interestOps(SelectionKey.OP_READ);
                    lock.notifyAll();
                }
            }
        }
    }

    private void readData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int bytesRead = channel.read(inBuffer);
        if (bytesRead > 0) {
            inBuffer.flip();
            byte[] array = new byte[bytesRead];
            inBuffer.get(array);
            inBuffer.compact();
            String message = new String(array).trim();
            message("\nMessage received from server: " + message);
        }
    }

    interface OnClientConnectedListener {
        void onClientConnected();
    }

    interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    interface OnErrorOccurredListener {
        void onErrorOccurred(Exception e);
    }

}
