package ru.mirea;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.List;

/**
 * Класс для обработки подключений от клиентов.
 */
public class Client {

    private final ByteBuffer bufferIn;
    private final ByteBuffer bufferOut;
    private final SelectionKey key;
    private final SocketChannel socket;
    private final String ipAddress;
    private final PacketReader packetReader;
    private final PacketProcessor packetProcessor;

    public Client(SocketChannel socket, SelectionKey key) {
        this.ipAddress = socket.socket().getInetAddress().getHostAddress();
        this.socket = socket;
        this.key = key;

        bufferIn = ByteBuffer.allocate(2048);
        bufferOut = ByteBuffer.allocate(2048);
        packetReader = new PacketReader(bufferIn);
        packetProcessor = new PacketProcessor();
        packetProcessor.setOnFileReceivedListener(new PacketProcessor.OnFileReceivedListener() {
            @Override
            public void onFileReceived(Path file) {
                Path reportPath = file.getParent().resolve(
                        String.format("%s-report.txt", file.getFileName().toString())
                );
                String analysisResult = FilesHelper.analyze(file);
                FilesHelper.writeSimpleFile(reportPath, analysisResult);
                sendMessage(analysisResult);
            }
        });
    }

    /**
     * Отправка данных клиенту.
     * @param message
     */
    public void sendMessage(String message) {
        bufferOut.put(message.getBytes());
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    /**
     * Обработка операций чтения.
     * @return Количество прочитанных байт.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public int handleRead() throws IOException, ClassNotFoundException {
        int bytesIn = socket.read(bufferIn);
        if (bytesIn == -1) {
            throw new IOException("Socket closed");
        }
        if (bytesIn > 0) {
            List<Packet> packets = packetReader.readPacket();
            packets.forEach(p -> {
                packetProcessor.processPacket(ipAddress, p);
            });
        }
        return bytesIn;
    }

    /**
     * Обработка операций записи.
     * @return Количество записанных байт.
     * @throws IOException
     */
    public int handleWrite() throws IOException {
        if (bufferOut.position() <= 0)
            return 0;
        bufferOut.flip();
        int bytesOut = socket.write(bufferOut);
        bufferOut.compact();
        if (bufferOut.position() > 0) {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
        return bytesOut;
    }

    public void disconnect() {
        try {
            socket.close();
            key.cancel();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
