package ru.mirea;

import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private static Client client;

    @BeforeAll
    static void setUp() {
        client = new Client();
        client.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenServerIsUp_thenClientIsRunning() {
        assertTrue(client.isRunning());
    }

    @Test
    void whenWrongPath_thenExceptionIsThrown() {
        assertThrows(RuntimeException.class, () -> { client.sendFile("unknown"); });
    }

    @Test
    void whenFileSend_thenReportReceived() {
        AtomicReference<String> received = new AtomicReference<>();
        client.setOnMessageReceivedListener(new Client.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) {
                received.set(message);
            }
        });
        try {
            Path p = Files.createTempFile("testfile1", ".txt");
            String testMessage = "Hello world";
            Files.write(p, testMessage.getBytes());
            client.sendFile(p.toFile().getAbsolutePath());
            TimeUnit.SECONDS.sleep(1);
            assertEquals(
                    String.format(
                            "\nMessage received from server: Filename: %s\nLines: %d, Words: %d, Symbols: %d",
                            p.getFileName().toString(),
                            1,
                            testMessage.split(" ").length,
                            testMessage.toCharArray().length
                    ),
                    received.get());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        client.stop();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}