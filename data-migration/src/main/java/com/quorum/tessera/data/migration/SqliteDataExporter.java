package com.quorum.tessera.data.migration;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    private static final String CREATE_TABLE_RESOURCE = "/ddls/sqlite-ddl.sql";

    @Override
    public void export(final StoreLoader loader, final Path output, final String username, final String password)
            throws SQLException, IOException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        Predicate<String> containsCreateEncryptedTransactionTable =
                line -> line.startsWith("CREATE TABLE ENCRYPTED_TRANSACTION");
        Predicate<String> containsCreateEncryptedRawTransactionTable =
                line -> line.startsWith("CREATE TABLE ENCRYPTED_RAW_TRANSACTION");

        final List<String> createTableStatements =
                Stream.of(getClass().getResourceAsStream(CREATE_TABLE_RESOURCE))
                        .map(InputStreamReader::new)
                        .map(BufferedReader::new)
                        .flatMap(BufferedReader::lines)
                        .filter(Objects::nonNull)
                        .filter(containsCreateEncryptedTransactionTable.or(containsCreateEncryptedRawTransactionTable))
                        .collect(Collectors.toList());

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                for (final String createTable : createTableStatements) {
                    stmt.executeUpdate(createTable);
                }
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
                DataEntry next;
                while ((next = loader.nextEntry()) != null) {
                    try (InputStream data = next.getValue()) {
                        insertStatement.setBytes(1, next.getKey());
                        insertStatement.setBytes(2, IOUtils.toByteArray(data));
                        insertStatement.execute();
                    }
                }
            }
        }
    }
}
