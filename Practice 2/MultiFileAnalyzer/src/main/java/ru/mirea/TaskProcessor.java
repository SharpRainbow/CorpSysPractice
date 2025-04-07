package ru.mirea;

/**
 * Базовый класс для классов, запускающих и управляющих задачами.
 * @param <T> Тип обрабатываемых задач.
 */
public interface TaskProcessor<T> {

    void process(T task);

    void process(Iterable<T> tasks);

}
