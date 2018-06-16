package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Proxy that acts as an interface to an HTTP Server.
 * Provides methods for creating the HTTP connection, writing a request and receiving the response.
 */
public class HttpProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxy.class);

    private final URI serverUri;

    private PrintWriter httpPrintWriter;

    private InputStreamReader httpInputStreamReader;

    private DataInputStream httpDataInputStream;

    private BufferedReader httpReader;

    /**
     * Connect to specified URL and create read/sendRequest streams.
     */
    public HttpProxy(URI uri) {

        Objects.requireNonNull(uri);
        serverUri = uri;
    }

    /**
     * Connect to the HTTP server.
     */
    public boolean connect() {
        try {
            Socket s = new Socket(serverUri.getHost(), serverUri.getPort());

            OutputStream httpOutputStream = s.getOutputStream();
            httpPrintWriter = new PrintWriter(httpOutputStream, true);

            InputStream httpInputStream = s.getInputStream();
            httpDataInputStream = new DataInputStream(httpInputStream);
            httpInputStreamReader = new InputStreamReader(httpInputStream); //############
            httpReader = new BufferedReader(httpInputStreamReader); //#####

            return true;

        } catch (ConnectException ex) {
            return false;

        } catch (IOException ex) {
            LOGGER.error("Failed to connect to URL: {}", serverUri);
            throw new RuntimeException(ex);
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
     * Parse an HTTP header content-length line to get the value.
     */
    public static int getContentLength(String headerLine) {

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(headerLine);

        if (matcher.find()) {
            String lengthStr = matcher.group();

            return Integer.valueOf(lengthStr);
        }

        return 0;
    }

    /**
     * Read response from the http connection.
     * Note that an http response will consist of multiple lines.
     */
    public String getResponse() {

        try {
            int contentLength = 0;
            StringBuilder header = new StringBuilder();
            String line;
            while ((line = httpReader.readLine()) != null && !line.equals("")) {
                LOGGER.info("Received HTTP line: {}", line);

                header.append(line + "\n");
                if (line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }
            }
            header.append("\n");
            LOGGER.info("Received HTTP header {}", header);
            LOGGER.info("Reading HTTP data ({} bytes)", contentLength);

            StringBuilder data = new StringBuilder();
            char[] arr = new char[contentLength];
            httpReader.read(arr, 0, arr.length);
            data.append(arr);
            LOGGER.info("Received HTTP data: {}", data.toString());

            return header.toString() + data.toString();

        } catch (IOException ex) {
            LOGGER.error("Failed to read from HTTP server");
            throw new RuntimeException(ex);
        }

    }

    /**
     * Main method for testing purposes only.
     */
    public static void main(final String... args) throws Exception {
        HttpProxy httpProxy = new HttpProxy(new URI("http://localhost" + ":" + "8080"));

        String message = "GET /upcheck HTTP/1.1\n" +
            "Host: c\n" +
            "User-Agent: Go-http-client/1.1\n" +
            "\n";
        httpProxy.sendRequest(new String(message));

        String line = httpProxy.getResponse();
        LOGGER.info("Received message: {}", line);
    }

}
