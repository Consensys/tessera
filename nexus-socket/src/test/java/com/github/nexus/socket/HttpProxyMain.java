
package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class HttpProxyMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyMain.class);

    /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {

        URI uri = new URI("http://localhost" + ":" + "8080");

        HttpProxy httpProxy = new HttpProxyFactory(uri).auth("off").create();

        if (httpProxy.connect()) {
            String message = "GET /upcheck HTTP/1.1\n" +
                "Host: c\n" +
                "User-Agent: Go-http-client/1.1\n" +
                "\n";
            httpProxy.sendRequest(message.getBytes());

            byte[] line = httpProxy.getResponse();
            LOGGER.info("Received message: {}", new String(line));

        } else {
            LOGGER.info("Failed to connect");

        }
    }
}
