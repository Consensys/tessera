package com.github.tessera.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class InputStreamUtils {

    //buffer size in bytes
    private static final int BUFFER_SIZE = 8192;

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
