package ru.mirea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Обработчик пакетов, полученных от клиентов.
 */
public class PacketProcessor {

    private OnFileReceivedListener onFileReceivedListener;

    /**
     * Позволяет установить слушатель на событие получение файла. Слушатель будет вызван только после получения целого файла.
     * @param onFileReceivedListener
     */
    public void setOnFileReceivedListener(OnFileReceivedListener onFileReceivedListener) {
        this.onFileReceivedListener = onFileReceivedListener;
    }

    /**
     * Метод, содержащий логику обработки пакета. Обрабатывает отедельный пакет данных.
     * @param sender Идентификатор получателя.
     * @param packet Пакет данных.
     */
    public void processPacket(String sender, Packet packet) {
        try {
            Path userDir = Path.of(sender);
            if (!Files.exists(userDir) || !Files.isDirectory(userDir)) {
                Files.createDirectory(userDir);
            }
            Path file = Path.of(sender, String.format("%d-%s", packet.getTimestamp(), packet.getFileName()));
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.toFile(), true))) {
                bos.write(packet.getChunkData());
                bos.flush();
            }
            if (packet.getFileSizeInBytes() == 0) {
                if (onFileReceivedListener != null) {
                    onFileReceivedListener.onFileReceived(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface OnFileReceivedListener {
        void onFileReceived(Path file);
    }

}
