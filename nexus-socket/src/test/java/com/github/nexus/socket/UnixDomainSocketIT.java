package com.github.nexus.socket;

import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixDomainSocketIT {

    private static final String CLIENT_MESSAGE_SENT = "Message sent by client";
    
    private static final String SERVER_MESSAGE_SENT = "Response sent by server";

    private UnixSocketFactory unixSocketFactory = UnixSocketFactory.create();
    
    
    @Test
    public void sendMessageToClient() {

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
        UnixDomainServerSocket serverUds;

        TestSocketServer() {
            serverUds = new UnixDomainServerSocket(unixSocketFactory);
            serverUds.create("/tmp", "tst2.ipc");
        }

        public void run() {

            try {
                //wait for a client to connect
                System.out.println("Waiting for client connection...");
                serverUds.connect();
                System.out.println("Client connection received");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            //sendRequest to client
            serverUds.write(SERVER_MESSAGE_SENT);

            //read response back
            String line = serverUds.read();
            assertThat(line).isEqualTo(CLIENT_MESSAGE_SENT);
        }
    }

}
