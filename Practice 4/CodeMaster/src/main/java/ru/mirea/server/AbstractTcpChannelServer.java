package ru.mirea.server;

import ru.mirea.client.Client;
import ru.mirea.client.ClientFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Абстрактный класс, содержащий логику сетевого взаимодействия.
 * Принимает подключения от клиентов, каждый из которых хранится в виде объекта Client.
 * Для обработки операций чтения и записи данный класс будет вызывать соответствующие методы
 * у подключенных клиентов. Принцип работы основан на использовании Selector.
 * В Selector добавляются каналы передачи данных клиентов с указанием требуемых операций.
 * Пока сервер запущен, Selector проверяет наличие каналов, готовых выполнить требуемыую операцию
 * и вызывает соответсвующие методы.
 */
public abstract class AbstractTcpChannelServer implements Server {

    private Selector selector;
    private final int SERVER_PORT;
    private final ClientFactory clientFactory;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<SelectionKey> writeRequests = new ConcurrentLinkedQueue<>();

    public AbstractTcpChannelServer(int port, ClientFactory clientFactory) {
        SERVER_PORT = port;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocketChannel inputChannel = ServerSocketChannel.open()) {
                    inputChannel.configureBlocking(false);
                    inputChannel.socket().bind(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
                    inputChannel.socket().setReuseAddress(true);
                    selector = SelectorProvider.provider().openSelector();
                    inputChannel.register(selector, SelectionKey.OP_ACCEPT);
                    running.set(true);
                    onServerMessage("Сервер ожидает подключений на порте " + SERVER_PORT);
                    SelectionKey writableKey;
                    while (running.get()) {
                        while ((writableKey = writeRequests.poll()) != null) {
                            if (writableKey.isValid())
                                writableKey.interestOps(writableKey.interestOps() | SelectionKey.OP_WRITE);
                        }
                        selector.select();
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (!key.isValid())
                                continue;
                            Client client = (Client) key.attachment();
                            try {
                                if (key.isAcceptable()) {
                                    accept(key);
                                }

                                if (key.isReadable()) {
                                    client.handleRead();
                                }

                                if (key.isWritable()) {
                                    client.handleWrite();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (client != null) {
                                    client.disconnect();
                                    key.cancel();
                                    onClientDisconnected(key);
                                }
                            }
                        }
                    }
                    selector.close();
                    onServerMessage("Сервер остановлен!");
                } catch (IOException e) {
                    onError(e);
                }
            }
        }).start();
    }

    protected abstract void onClientDisconnected(SelectionKey clientKey);

    protected abstract void onClientConnected(SelectionKey clientKey) throws IOException;

    protected abstract void onError(Exception e);

    protected abstract void onServerMessage(String message);

    public void sendBroadcastMessage(String message) {
        for (SelectionKey key : selector.keys()) {
            if (!key.isValid())
                continue;
            Client client = (Client) key.attachment();
            if (client == null)
                continue;
            client.sendMessage(message);
            addWriteRequest(key);
        }
        selector.wakeup();
    }

    public void addWriteRequest(SelectionKey key) {
        writeRequests.add(key);
    }

    private void accept(SelectionKey serverKey) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) serverKey.channel();
        SocketChannel socket = channel.accept();
        socket.configureBlocking(false);
        SelectionKey clientKey = socket.register(serverKey.selector(), SelectionKey.OP_READ);
        Client client = clientFactory.createClient(
                socket.socket().getInetAddress().getHostAddress(), socket
        );
        clientKey.attach(client);
        client.sendMessage(client.toString());
        clientKey.interestOps(SelectionKey.OP_WRITE);
        onClientConnected(clientKey);
    }

    @Override
    public void stop() {
        if (selector != null) {
            selector.wakeup();
        }
        running.set(false);
    }

}
