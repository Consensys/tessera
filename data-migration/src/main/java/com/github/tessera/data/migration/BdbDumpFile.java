package com.github.tessera.data.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class BdbDumpFile {

    private final Path inputFile;

    public BdbDumpFile(Path inputFile) {
        this.inputFile = inputFile;
    }

    public void execute(OutputStream outputStream) throws IOException {

        String sql = "INSERT INTO ENCRYPTED_TRANSACTION (ENC_TX_SEQ,HASH,ENCODED_PAYLOAD) VALUES (ENC_TX_SEQ.NEXTVAL,'%s','%s');";

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
                BufferedWriter writer = Stream.of(outputStream)
                        .map(OutputStreamWriter::new)
                        .map(BufferedWriter::new)
                        .findAny().get()) {

            while (true) {
                String line = reader.readLine();
                if (Objects.isNull(line)) {
                    break;
                }

                if (!line.startsWith(" ")) {
                    continue;
                }
                final String key = line;
                final String value = reader.readLine();

                final String insertLine = String.format(sql, key.trim(), value.trim());
                writer.write(insertLine);
                writer.newLine();

            }
        }
    }

}
