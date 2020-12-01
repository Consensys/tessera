package com.quorum.tessera.test.vault;

import java.io.*;
import java.util.stream.Stream;

public class StreamConsumer implements Runnable {

    private InputStream inputStream;

    public StreamConsumer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try (BufferedReader reader =
                 Stream.of(inputStream)
                     .map(InputStreamReader::new)
                     .map(BufferedReader::new)
                     .findAny()
                     .get()) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("LINEOUT "+ line);
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
