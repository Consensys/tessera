package com.github.nexus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClientConnection extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClientConnection.class);

    private UnixDomainClientSocket uds;

    /**
     * Initialize the streams and start the thread
     * TODO: should pull the hard-coded details from config.
     */
    public SocketClientConnection() {
        uds = new UnixDomainClientSocket();
        uds.connect("/tmp", "tst.ipc");
        this.start();
    }

    /**
     * Test - read data from socket and log it & write a dummy response back.
     */
    public void run() {

        for(;;) {
            String line = uds.read();
            if (line == null) {
                break;
            }

            // reverse it
            int len = line.length();
            StringBuffer revline = new StringBuffer(len);
            for(int i = len-1; i >= 0; i--) {
                revline.insert(len-1-i, line.charAt(i));
            }

            // and write out the reversed line
            uds.write(new String(revline));
        }
    }
}
