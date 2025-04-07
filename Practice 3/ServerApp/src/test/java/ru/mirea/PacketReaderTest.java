package ru.mirea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacketReaderTest {

    private PacketReader packetReader;
    private ByteBuffer byteBuffer;

    @BeforeEach
    void setUp() {
        byteBuffer = ByteBuffer.allocate(256);
        packetReader = new PacketReader(byteBuffer);
    }

    @Test
    void whenBufferHasPacket_thenCanBeReadCorrectly() {
        Packet packet = new Packet(
                "test",
                1,
                1,
                new byte[]{1}
        );
        try {
            writeObject(packet);
            List<Packet> packets = packetReader.readPacket();
            assertEquals(1, packets.size());
            assertEquals(packet, packets.get(0));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenBufferHasUnknownObject_thenExceptionIsThrown() {
        String string = "data packet";
        try {
            writeObject(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThrows(ClassCastException.class, () -> packetReader.readPacket());
    }

    private void writeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(o);
        byte[] bytes = baos.toByteArray();
        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
    }

    @AfterEach
    void tearDown() {
    }
}