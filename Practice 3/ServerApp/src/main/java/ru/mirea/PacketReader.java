package ru.mirea;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, позволяющий получать пакеты данных из потока байт.
 */
public class PacketReader {

    private final ByteArrayInputStream byteToStreamConverter;
    private ObjectInputStream streamToObjectConverter;
    private ByteBuffer sourceBuffer;

    /**
     * Конструктор класса.
     * @param byteBuffer Буфер, из которого будут считываться пакеты даных.
     */
    public PacketReader(ByteBuffer byteBuffer) {
        this.sourceBuffer = byteBuffer;
        byteToStreamConverter = new ByteArrayInputStream(byteBuffer.array());
    }

    public List<Packet> readPacket() throws IOException, ClassNotFoundException {
        ArrayList<Packet> packets = new ArrayList<>();
        int packetLength = sourceBuffer.getInt(0);
        while (packetLength > 0 && packetLength <= sourceBuffer.position()) {
            sourceBuffer.flip();
            sourceBuffer.position(packetLength + 4);
            byteToStreamConverter.reset();
            byteToStreamConverter.skip(4);
            if (streamToObjectConverter == null)
                streamToObjectConverter = new ObjectInputStream(byteToStreamConverter);
            Packet packet = (Packet) streamToObjectConverter.readObject();
            packets.add(packet);
            sourceBuffer.compact();
            byteToStreamConverter.reset();
            packetLength = sourceBuffer.getInt(0);
        }
        return packets;
    }

}
