package ru.mirea;

/**
 * Базовый класс для фабрик, позволяющих создавать задачи для запуска в новом потоке.
 */
public abstract class TaskFactory {

    public RunnableTask create(String description, RunnableTask.OnTaskCompleteListener listener) {
        RunnableTask task = createTask(description);
        task.setOnTaskCompleteListener(listener);
        return task;
    }

    protected abstract RunnableTask createTask(String description);

}
