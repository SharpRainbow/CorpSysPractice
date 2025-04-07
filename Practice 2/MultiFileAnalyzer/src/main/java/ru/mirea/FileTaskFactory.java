package ru.mirea;

/**
 * Базовый интерфейс для фабрик, позволяющих создавать задачи анализатора файлов.
 */
public interface FileTaskFactory{

    FileTask createTask(String fileName);

}
