
package com.github.nexus.socket;

import java.net.URI;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class HttpProxyMain {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyMain.class);
    
      /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {
        SocketFactory socketFactory = new SocketFactory();
        
        HttpProxy httpProxy = new HttpProxy(new URI("http://localhost" + ":" + "8080"),socketFactory);

        if (httpProxy.connect()) {
            String message = "GET /upcheck HTTP/1.1\n" +
                "Host: c\n" +
                "User-Agent: Go-http-client/1.1\n" +
                "\n";
            httpProxy.sendRequest(new String(message));

            String line = httpProxy.getResponse();
            LOGGER.info("Received message: {}", line);

        } else {
            LOGGER.info("Failed to connect");

        }
    }  
}
