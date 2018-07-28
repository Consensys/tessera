
package com.quorum.tessera.socket;


import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpProxyMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyMain.class);

    /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {

        URI uri = new URI("http://localhost:8080");

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getServerUri()).thenReturn(uri);
        when(serverConfig.isSsl()).thenReturn(false);
        HttpProxy httpProxy = new HttpProxyFactory(serverConfig).create();

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
