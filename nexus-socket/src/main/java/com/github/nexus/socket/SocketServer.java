package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Create a server listening on a Unix Domain Socket and processing http requests
 * received over the socket.
 */
public class SocketServer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    UnixDomainServerSocket serverUds;

    private UnixDomainServerSocket serverSocket;

    /**
     * Initialize the streams and start the thread
     * TODO: should pull the hard-coded details from config.
     */
    public SocketServer() {

        serverUds = new UnixDomainServerSocket();
        serverUds.create("/tmp", "tst1.ipc");

        this.start();
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

        while (true) {
            String line = serverUds.read();
            LOGGER.info("Received message: {}", line);
        }
    }

}
