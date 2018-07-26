package com.quorum.tessera.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Utility functions for dealing with input streams
 */
public class InputStreamUtils {

    //buffer size in bytes
    private static final int BUFFER_SIZE = 8192;

    /**
     * Reads an entire input stream in 8kb chunks
     *
     * @param is the input stream to read
     * @return the full set of data read from the stream
     * @throws IOException if there was an issue interaction with the input stream
     */
    static byte[] readAllBytes(final InputStream is) throws IOException {

        final ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);

        int read;

        do {

            final byte[] buff = new byte[BUFFER_SIZE];
            read = is.read(buff);

            os.write(Arrays.copyOf(buff, read));

        } while (read == BUFFER_SIZE);

        return os.toByteArray();
    }

}
