package ru.mirea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PacketProcessorTest {

    private static final String TEST_DIR_NAME = "test";
    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_MESSAGE = "Hello world";
    private PacketProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PacketProcessor();
    }

    @Test
    void whenFirstPacketSubmitted_thenFileIsCreated() {
        long time = System.currentTimeMillis();
        Packet packet = new Packet(
                TEST_FILE_NAME,
                time,
                1,
                TEST_MESSAGE.getBytes()
        );
        String fullFileName = String.format("%d-%s", time, TEST_FILE_NAME);
        Path p = Path.of(TEST_DIR_NAME, fullFileName);
        processor.processPacket(TEST_DIR_NAME, packet);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(p.toAbsolutePath());
        assertTrue(Files.exists(p));
    }

    @Test
    void whenLastPacketSubmitted_thenReceiverIsFired() {
        AtomicReference<Path> receivedFile = new AtomicReference<>();
        processor.setOnFileReceivedListener(new PacketProcessor.OnFileReceivedListener() {
            @Override
            public void onFileReceived(Path file) {
                receivedFile.set(file);
            }
        });
        long time = System.currentTimeMillis();
        Packet packet = new Packet(
                TEST_FILE_NAME,
                time,
                0,
                TEST_MESSAGE.getBytes()
        );
        String fullFileName = String.format("%d-%s", time, TEST_FILE_NAME);
        Path p = Path.of(TEST_DIR_NAME, fullFileName);
        processor.processPacket(TEST_DIR_NAME, packet);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(receivedFile.get(), p);
    }

    @Test
    void whenSeveralPacketsReceived_thenDataIsAppendedToFile() {
        long time = System.currentTimeMillis();
        Packet packet1 = new Packet(
                TEST_FILE_NAME,
                time,
                1,
                TEST_MESSAGE.getBytes()
        );
        Packet packet2 = new Packet(
                TEST_FILE_NAME,
                time,
                1,
                TEST_MESSAGE.getBytes()
        );
        String fullFileName = String.format("%d-%s", time, TEST_FILE_NAME);
        Path p = Path.of(TEST_DIR_NAME, fullFileName);
        processor.processPacket(TEST_DIR_NAME, packet1);
        processor.processPacket(TEST_DIR_NAME, packet2);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = Files.newBufferedReader(p)) {
            assertEquals(String.format("%s%s", TEST_MESSAGE, TEST_MESSAGE), reader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        Path p = Path.of(TEST_DIR_NAME);
        try {
            if (Files.exists(p)) {
                try (Stream<Path> paths = Files.walk(p)) {
                    paths.map(Path::toFile).forEach(File::delete);
                }
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}