package ru.mirea.client;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Интерфейс, определяющий функции клиента.
 * Содержит вложенный интерфейс, позволяющий
 * расширять возможности клиента с помощью установки
 * слушателей на операции чтения и записи.
 */
public interface Client {

    int handleRead() throws IOException;

    int handleWrite()throws IOException;

    void disconnect() throws IOException;

    void sendMessage(String message);

    void setReadWriteListener(ReadWriteListener readWriteListener);

    interface ReadWriteListener {

        void onDataRead(ByteBuffer buffer);

        void onDataWrite(ByteBuffer buffer);

    }
}
