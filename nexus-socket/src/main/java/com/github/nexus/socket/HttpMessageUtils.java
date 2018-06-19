package com.github.nexus.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpMessageUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageUtils.class);

    private HttpMessageUtils() {
        throw new UnsupportedOperationException();
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
     * Read an HTTP request or response from an input stream.
     */
    public static String getHttpMessage(BufferedReader reader) {

        try {
            int contentLength = 0;
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.equals("")) {
                LOGGER.debug("Received HTTP line: {}", line);

                response.append(line + "\n");
                if (line.contains("Content-Length")) {
                    contentLength = HttpMessageUtils.getContentLength(line);
                }
            }
            response.append("\n");
            LOGGER.debug("Received HTTP header {}", response);
            LOGGER.debug("Reading {} bytes of data payload)", contentLength);

            char[] arr = new char[contentLength];
            reader.read(arr, 0, arr.length);
            response.append(arr);

            LOGGER.info("Received HTTP message: {}", response.toString());
            return response.toString();

        } catch (IOException ex) {
            LOGGER.error("Failed to read from HTTP server");
            throw new NexusSocketException(ex);
        }

    }
}
