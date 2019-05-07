package com.quorum.tessera.data.migration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    @Override
    public void export(final StoreLoader loader,
                       final Path output,
                       final String username,
                       final String password) throws SQLException, IOException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        final byte[] sqlData = IOUtils.resourceToByteArray("/ddls/sqlite-ddl.sql");
        final String dataAsString = new String(sqlData, UTF_8);
        final String[] createTableStatements = dataAsString.split("\n");

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
