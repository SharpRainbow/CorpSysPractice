package ru.mirea;

/**
 * Базовый интерфейс для анализатора данных.
 * @param <T> Тип данных, который будет проанализирован.
 * @param <R> Тип данных, который будет использован для возврата результата анализа.
 */
public interface ContentAnalyzer<T, R> {

    /**
     * Данный метод необходимо вызвать перед началом анализа.
     * @param contentDescription Описание анализируемых данных.
     */
    void prepare(String contentDescription);

    /**
     * Данный метод должен содержать логику анализа данных.
     * @param content Данные для анализа.
     */
    void analyzePart(T content);

    /**
     * Возврат результата анализа.
     * @return Результат анализа данных.
     */
    R generateResult();

}
