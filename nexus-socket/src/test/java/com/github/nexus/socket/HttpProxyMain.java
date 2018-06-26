
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HttpProxyMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyMain.class);

    /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {

        URI uri = new URI("http://localhost:8080");

        Configuration config = mock(Configuration.class);
        doReturn(uri).when(config).uri();
        doReturn("off").when(config).tls();

        HttpProxy httpProxy = new HttpProxyFactory(config).create();

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
