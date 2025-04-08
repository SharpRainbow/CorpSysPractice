package ru.mirea;

public abstract class RunnableTask implements Runnable {

    protected OnTaskCompleteListener onTaskCompleteListener;

    /**
     * Данный метод позволяет установить слущатель завершения задачи.
     * @param onTaskCompleteListener Реализация интерфейса слушателя.
     */
    public void setOnTaskCompleteListener(OnTaskCompleteListener onTaskCompleteListener) {
        this.onTaskCompleteListener = onTaskCompleteListener;
    }

    interface OnTaskCompleteListener {
        void onTaskComplete(FileAnalysis analysis);
    }

}
