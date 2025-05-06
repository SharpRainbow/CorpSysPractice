package ru.mirea.game;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mirea.ClientActions;
import ru.mirea.client.Client;
import ru.mirea.client.TcpChannelClient;
import ru.mirea.logger.CodeMasterLogRecord;
import ru.mirea.logger.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionManagerTest {

    private GameSessionManager sessionManager;
    private String broadcastMessage = "";
    private ByteChannel mockChannel;

    @BeforeEach
    void setUp() {
        sessionManager = new GameSessionManager(
                o -> { },
                new GameConfig(
                        2, 4, 0, 4
                )
        );
        sessionManager.subscribeBroadcastMessage(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String message = (String) evt.getNewValue();
                broadcastMessage = message;
            }
        });
        mockChannel = new ByteChannel() {
            @Override
            public int read(ByteBuffer dst) throws IOException {
                return 0;
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return 0;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() throws IOException {

            }
        };
    }

    @Test
    void whenPlayerIsConnected_thenBroadcastIsSent() {
        sessionManager.playerConnected();
        assertFalse(broadcastMessage.isEmpty());
    }

    @Test
    void whenNPlayersConnected_thenConnectedPlayersCountIsCorrect() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        assertEquals(2, sessionManager.getConnectedPlayers());
    }

    @Test
    void whenPlayerIsConnected_andNPlayersAlreadyConnected_thenConnectionIsDeclined() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        boolean connected = sessionManager.playerConnected();
        assertFalse(connected);
        assertEquals(4, sessionManager.getConnectedPlayers());
    }

    @Test
    void whenPlayerDisconnected_thenBroadcastIsSent() {
        sessionManager.playerConnected();
        broadcastMessage = "";
        sessionManager.playerDisconnected(new TcpChannelClient("", mockChannel));
        assertFalse(broadcastMessage.isEmpty());
    }

    @Test
    void whenNPlayersDisconnected_thenConnectedPlayersCountIsCorrect() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        sessionManager.playerDisconnected(new TcpChannelClient("", mockChannel));
        sessionManager.playerDisconnected(new TcpChannelClient("", mockChannel));
        assertEquals(1, sessionManager.getConnectedPlayers());
    }

    @Test
    void whenPlayerIsDisconnected_andConnectedPlayersCountIsEqualToReadyPlayersCount_thenGameIsRunning() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        Client client1 = new TcpChannelClient("", mockChannel);
        Client client2 = new TcpChannelClient("", mockChannel);
        sessionManager.processClientMessage(client1, ClientActions.READY.getName());
        sessionManager.processClientMessage(client2, ClientActions.READY.getName());
        assertFalse(sessionManager.isGameRunning());
        sessionManager.playerDisconnected(new TcpChannelClient("", mockChannel));
        assertTrue(sessionManager.isGameRunning());
    }

    @Test
    void whenPlayerIsDisconnected_andPlayersCountIsLowerThanMinPlayersCount_thenGameIsNotRunning() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        Client client1 = new TcpChannelClient("", mockChannel);
        Client client2 = new TcpChannelClient("", mockChannel);
        sessionManager.processClientMessage(client1, ClientActions.READY.getName());
        sessionManager.processClientMessage(client2, ClientActions.READY.getName());
        sessionManager.playerDisconnected(client2);
        assertFalse(sessionManager.isGameRunning());
    }

    @Test
    void whenPlayerGuessIsCorrect_thenBroadcastIsSent() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        Client client1 = new TcpChannelClient("", mockChannel);
        Client client2 = new TcpChannelClient("", mockChannel);
        sessionManager.processClientMessage(client1, ClientActions.READY.getName());
        sessionManager.processClientMessage(client2, ClientActions.READY.getName());
        broadcastMessage = "";
        sessionManager.processClientMessage(client1, "1234");
        assertFalse(broadcastMessage.isEmpty());
    }

    @Test
    void whenPlayerGuessIsCorrect_thenGameIsNotRunning() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        Client client1 = new TcpChannelClient("", mockChannel);
        Client client2 = new TcpChannelClient("", mockChannel);
        sessionManager.processClientMessage(client1, ClientActions.READY.getName());
        sessionManager.processClientMessage(client2, ClientActions.READY.getName());
        broadcastMessage = "";
        sessionManager.processClientMessage(client1, "1234");
        assertFalse(sessionManager.isGameRunning());
    }

    @Test
    void whenAllConnectedPlayersAreReady_andConnectedPlayersCountIsEqualOrHigherThanMinPlayersCount_thenGameIsRunning() {
        sessionManager.playerConnected();
        sessionManager.playerConnected();
        Client client1 = new TcpChannelClient("", mockChannel);
        Client client2 = new TcpChannelClient("", mockChannel);
        sessionManager.processClientMessage(client1, ClientActions.READY.getName());
        sessionManager.processClientMessage(client2, ClientActions.READY.getName());
        assertTrue(sessionManager.isGameRunning());
    }

    @AfterEach
    void tearDown() {
        if (sessionManager != null) {
            sessionManager.endSession();
        }
    }

}