package ru.mirea;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Основной класс, содержащий логику работы сервера.
 */
public class Server {

    private static final int SERVER_PORT = 5555;
    private static final String SERVER_ADDRESS = "0.0.0.0";
    private OnErrorOccurredListener onErrorOccurredListener;
    private OnServerMessageListener onServerMessageListener;
    private final AtomicBoolean running;
    private Selector selector;

    /**
     * Метод позволяющий установить слушатель на событие ошибки.
     * @param onErrorOccurredListener
     */
    public void setOnErrorOccurredListener(OnErrorOccurredListener onErrorOccurredListener) {
        this.onErrorOccurredListener = onErrorOccurredListener;
    }

    /**
     * Метод позволяющий установить слушатель на получение логов от сервера.
     * @param onServerMessageListener
     */
    public void setOnServerMessageListener(OnServerMessageListener onServerMessageListener) {
        this.onServerMessageListener = onServerMessageListener;
    }

    public Server() {
        running = new AtomicBoolean(false);
    }

    /**
     * Запуск сервера.
     * Сервер запускается в новом потоке во избежание блокировки вызывающего потока. Все соединения от клиентов
     * обрабатываются в едином запущенном потоке в неблокирующем режиме с помощью Selector.
     * <p>
     * При запуске сервера в Selector регистрируется ServerSocketChannel с указанием события ACCEPT. Данное действие
     * позволяет получать оповещения о наличии новых подключений от пользователей при каждом опросе селектора. После
     * подключения клиента, объект SocketChannel, отвечающий за соединение с клиентом, также регистрируется в селекторе
     * с указанием опции READ для получения сообщений.
     */
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
                    message("Server started on port " + SERVER_PORT);
                    while (running.get()) {
                        selector.select();
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = (SelectionKey) iterator.next();
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
                                client.disconnect();
                            }
                        }
                    }
                    selector.close();
                    message("Server stopped!");
                } catch (IOException e) {
                    if (onErrorOccurredListener != null)
                        onErrorOccurredListener.onError(e);
                    else
                        throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void stop() {
        selector.wakeup();
        running.set(false);
    }

    private void message(String message) {
        if (onServerMessageListener != null) {
            onServerMessageListener.onServerMessage(message);
        }
    }

    /**
     * Метод для установления соединения с клиентом.
     * @param key Ключ, по которому хранится объект ServerSocket, принимающий соединения
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel socket = channel.accept();
        socket.configureBlocking(false);
        SelectionKey k = socket.register(key.selector(), SelectionKey.OP_READ);
        k.attach(new Client(socket, k));
    }

    interface OnErrorOccurredListener {
        void onError(Exception e);
    }

    interface OnServerMessageListener {
        void onServerMessage(String message);
    }

}
