package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;

/**
 * Proxy that acts as an interface to an HTTP Server.
 * Provides methods for creating the HTTP connection, writing a request and receiving the response.
 */
public class HttpProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxy.class);

    private PrintWriter httpPrintWriter;

    private BufferedReader httpReader;

    /**
     * Connect to specified URL and create read/write connections.
     */
    public HttpProxy(URL url) {

        try {
            Socket s = new Socket(url.getHost(), url.getPort());
            OutputStream httpOutputStream = s.getOutputStream();
            httpPrintWriter = new PrintWriter(httpOutputStream, true);

            InputStream httpInputStream = s.getInputStream();
            InputStreamReader httpInputStreamReader = new InputStreamReader(httpInputStream);
            httpReader = new BufferedReader(httpInputStreamReader);

        } catch (IOException ex) {
            LOGGER.error("Failed to connect to URL: {}", url);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Write data to the http connection.
     */
    public void write (String data) {
        LOGGER.info("Sending HTTP request: {}", data);
        httpPrintWriter.write(data);
        httpPrintWriter.flush();
    }

    /**
     * Read one line of data from the http connection.
     * Note that an http response will consist of multiple lines.
     */
    public String readLine() {

        try {
            return httpReader.readLine();
        } catch (IOException ex) {
            LOGGER.error("Read from HTTP connection failed");
            throw new RuntimeException(ex);
        }

    }

    /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {
        HttpProxy httpProxy = new HttpProxy(new URL("http://localhost:8080"));

        String message = "GET /upcheck HTTP/1.1\n" +
            "Host: c\n" +
            "User-Agent: Go-http-client/1.1\n" +
            "\n";
        httpProxy.write(new String(message));

        String line = httpProxy.readLine();
        System.out.println("Received message: " + line);
    }

}
