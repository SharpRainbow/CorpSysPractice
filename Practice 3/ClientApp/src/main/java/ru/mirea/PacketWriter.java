package ru.mirea;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class PacketWriter {

    private final ByteArrayOutputStream baos;
    private ObjectOutputStream objectStream;

    public PacketWriter() {
        baos = new ByteArrayOutputStream();
    }

    public void setup() {
        baos.reset();
        try {
            objectStream = new ObjectOutputStream(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writePacket(Packet packet, ByteBuffer outBuffer) throws IOException {
        if (objectStream == null) {
            return;
        }
        objectStream.writeObject(packet);
        byte[] data = baos.toByteArray();
        outBuffer.putInt(data.length);
        outBuffer.put(data);
        objectStream.close();
        baos.reset();
    }

}
