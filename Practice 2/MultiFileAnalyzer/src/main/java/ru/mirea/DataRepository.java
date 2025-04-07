package ru.mirea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Базовый интерфейс для хранилища данных.
 */
public interface DataRepository {

    /**
     * Получение потока данных из файла.
     * @param source Идентификатор для поиска данных.
     * @return
     * @throws IOException
     */
    InputStream getStream(String source) throws IOException;

    /**
     * Получение объекта Reader для данных, позволяющего читать символы и строки.
     * @param source Идентификатор для поиска данных.
     * @return
     * @throws IOException
     */
    BufferedReader getReader(String source) throws IOException;

}
