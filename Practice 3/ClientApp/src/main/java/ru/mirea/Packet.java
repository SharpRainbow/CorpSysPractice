package ru.mirea;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Packet implements Serializable {

    private String fileName;
    private long timestamp;
    private long fileSizeInBytes;
    private byte[] chunkData;

    private static final long serialVersionUID = 1;

    public Packet(String fileName, long timestamp, long fileSizeInBytes, byte[] chunkData) {
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.fileSizeInBytes = fileSizeInBytes;
        this.chunkData = chunkData;
    }

    public String getFileName() {
        return fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return timestamp == packet.timestamp
                && fileSizeInBytes == packet.fileSizeInBytes
                && Objects.equals(fileName, packet.fileName)
                && Objects.deepEquals(chunkData, packet.chunkData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, timestamp, fileSizeInBytes, Arrays.hashCode(chunkData));
    }

    @Override
    public String toString() {
        return "Packet{" +
                "fileSizeInBytes=" + fileSizeInBytes +
                ", timestamp=" + timestamp +
                ", fileName='" + fileName + '\'' +
                ", chunkDataSize=" + chunkData.length +
                '}';
    }
}
