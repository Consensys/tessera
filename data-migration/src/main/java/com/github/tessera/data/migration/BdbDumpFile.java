package com.github.tessera.data.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assumes that user has exported data from bdb using db_dump
 * <pre>
 *  db_dump -f exported.txt c1/cn§.db/playload.db
 * </pre>
 *
 */
public class BdbDumpFile {

    private final Path inputFile;

    public BdbDumpFile(Path inputFile) {
        this.inputFile = inputFile;
    }

    public void execute(Path outputFile) throws IOException, SQLException {

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + outputFile.toString())) {

            try (Statement createTableStatement = conn.createStatement()) {
                createTableStatement.execute("CREATE TABLE IF NOT EXISTS ENCRYPTED_TRANSACTION (ID BIGINT,HASH BLOB,ENCODED_PAYLOAD BLOB)");
                // createTableStatement.execute("CREATE IF NOT EXISTS  SEQUENCE ENC_TX_SEQ");
            }

            try (BufferedReader reader = Files.newBufferedReader(inputFile)) {

                while (true) {
                    String line = reader.readLine();
                    if (Objects.isNull(line)) {
                        break;
                    }

                    if (!line.startsWith(" ")) {
                        continue;
                    }

                    final String key = line.trim();

                    final String value = reader.readLine();

                    AtomicInteger counter = new AtomicInteger(1);
                    try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO ENCRYPTED_TRANSACTION VALUES(?, ?,?)")) {
                        insertStatement.setLong(1, counter.incrementAndGet());
                        insertStatement.setBytes(2, Base64.getDecoder().decode(key));

                        insertStatement.setBytes(3, value.getBytes(StandardCharsets.US_ASCII));
                        insertStatement.execute();
                    }

                }

            }
        }
    }

}
