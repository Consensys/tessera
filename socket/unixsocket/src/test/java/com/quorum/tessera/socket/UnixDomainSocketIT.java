package com.quorum.tessera.socket;

import com.quorum.tessera.socket.client.UnixDomainClientSocket;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixDomainSocketIT {

    private static final String CLIENT_MESSAGE_SENT = "Message sent by client";

    private static final String SERVER_MESSAGE_SENT = "Response sent by server";

    private UnixSocketFactory unixSocketFactory = UnixSocketFactory.create();

    @Test
    public void sendMessageToClient() throws IOException {

        //Create a server which is listening on the socket
        TestSocketServer server = new TestSocketServer();
        server.start();

        //Create a client which will send a message
        UnixDomainClientSocket clientUds = new UnixDomainClientSocket(unixSocketFactory);
        clientUds.connect("/tmp", "tst2.ipc");

        //read message sent by server
        String line = clientUds.read();
        assertThat(line).isEqualTo(SERVER_MESSAGE_SENT);

        //send message back to server
        clientUds.write(CLIENT_MESSAGE_SENT);
    }

    /**
     * Server listener thread
     */
    class TestSocketServer extends Thread {

        final ServerSocket server;

        TestSocketServer() throws IOException {
            this.server = unixSocketFactory.createServerSocket(Paths.get("/tmp", "tst2.ipc"));
        }

        public void run() {
            try {

                //wait for a client to connect
                System.out.println("Waiting for client connection...");
                final Socket socket = server.accept();

                final UnixDomainServerSocket udss = new UnixDomainServerSocket(socket);

                System.out.println("Client connection received");

                //sendRequest to client
                udss.write(SERVER_MESSAGE_SENT.getBytes());

                //read response back
                byte[] line = udss.read();
                assertThat(line).isEqualTo(CLIENT_MESSAGE_SENT);

            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
