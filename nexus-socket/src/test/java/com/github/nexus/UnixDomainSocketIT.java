package com.github.nexus;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixDomainSocketIT {

    final static private String CLIENT_MESSAGE_SENT = "Message sent by client";
    final static private String SERVER_MESSAGE_SENT = "Response sent by server";

    @Test
    public void sendMessageToClient() {

        //Create a server which is listening on the socket
        SocketServer server = new SocketServer();
        server.start();

        //Create a client which will send a message
        UnixDomainClientSocket clientUds = new UnixDomainClientSocket();
        clientUds.connect("/tmp", "tst1.ipc");

        //read message sent by server
        String line = clientUds.read();
        assertThat(line).isEqualTo(SERVER_MESSAGE_SENT);

        //send message back to server
        clientUds.write(CLIENT_MESSAGE_SENT);
    }


    /**
     * Server listener thread
     */
    class SocketServer extends Thread {
        UnixDomainServerSocket serverUds;

        public SocketServer() {
            serverUds = new UnixDomainServerSocket();
            serverUds.create("/tmp", "tst1.ipc");
        }

        public void run() {
            //wait for a client to connect
            System.out.println("Waiting for client connection...");
            serverUds.connect();
            System.out.println("Client connection received");

            //write to client
            serverUds.write(SERVER_MESSAGE_SENT);

            //read response back
            String line = serverUds.read();
            assertThat(line).isEqualTo(CLIENT_MESSAGE_SENT);
        }
    }

}
