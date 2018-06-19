package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Objects;

/**
 * Proxy that acts as an interface to an HTTP Server.
 * Provides methods for creating the HTTP connection, writing a request and receiving the response.
 */
public class HttpProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxy.class);

    private final URI serverUri;

    private Socket socket;

    private PrintWriter httpPrintWriter;

    private BufferedReader httpReader;
    
    
    private SocketFactory socketFactory;
    
    /**
     * Connect to specified URL and create read/sendRequest streams.
     */
    public HttpProxy(URI serverUri,SocketFactory socketFactory) {
        this.socketFactory = Objects.requireNonNull(socketFactory);
        this.serverUri = Objects.requireNonNull(serverUri);
    }

    /**
     * Connect to the HTTP server.
     */
    public boolean connect() {
        try {
            socket = socketFactory.create(serverUri);

            OutputStream httpOutputStream = socket.getOutputStream();
            httpPrintWriter = new PrintWriter(httpOutputStream, true);

            InputStream httpInputStream = socket.getInputStream();
            InputStreamReader httpInputStreamReader = new InputStreamReader(httpInputStream);
            httpReader = new BufferedReader(httpInputStreamReader);

            return true;

        } catch (ConnectException ex) {
            return false;

        } catch (IOException ex) {
            LOGGER.error("Failed to connect to URL: {}", serverUri);
            throw new NexusSocketException(ex);
        }
    }

    /**
     * Disconnect from HTTP server and clean up.
     */
    public void disconnect() {
        try {
            httpPrintWriter.close();
            httpReader.close();
            socket.close();

        } catch (IOException ex) {
            LOGGER.info("Ignoring exception on HttpProxy disconnect: {}", ex.getMessage());
        }
    }

    /**
     * Write data to the http connection.
     */
    public void sendRequest(String data) {
        LOGGER.info("Sending HTTP request: {}", data);
        httpPrintWriter.write(data);
        httpPrintWriter.flush();
    }

    /**
     * Read response from the http connection.
     * Note that an http response will consist of multiple lines.
     */
    public String getResponse() {

        return HttpMessageUtils.getHttpMessage(httpReader);
    }



}
