package ru.mirea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PacketWriterTest {

    private PacketWriter packetWriter;

    @BeforeEach
    void setUp() {
        packetWriter = new PacketWriter();
        packetWriter.setup();
    }

    @Test
    void whenPacketWritten_thenShouldBeAbleToRetrieve() {
        Packet packet = new Packet(
                "test",
                1,
                1,
                new byte[]{1}
        );
        ByteBuffer buffer = ByteBuffer.allocate(256);
        try {
            packetWriter.writePacket(packet, buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new ByteArrayInputStream(
                            Arrays.copyOfRange(buffer.array(), 4, buffer.position())
                    )
            );
            Packet retrievedPacket = (Packet) objectInputStream.readObject();
            assertEquals(packet, retrievedPacket);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenProvidedBufferDoNotHaveSpace_thenExceptionIsThrown() {
        Packet packet = new Packet(
                "test",
                1,
                1,
                new byte[]{1}
        );
        ByteBuffer buffer = ByteBuffer.allocate(8);
        assertThrows(BufferOverflowException.class, () -> { packetWriter.writePacket(packet, buffer); });
    }
}