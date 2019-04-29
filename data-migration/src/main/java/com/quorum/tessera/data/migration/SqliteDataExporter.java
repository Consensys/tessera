package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.io.UriCallback;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    @Override
    public void export(final StoreLoader loader,
                       final Path output,
                       final String username,
                       final String password) throws SQLException, IOException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        final URI sqlFile = UriCallback.execute(() -> getClass().getResource("/ddls/sqlite-ddl.sql").toURI());

        final List<String> createTables = IOCallback.execute(() -> Files.readAllLines(Paths.get(sqlFile)));

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                for (final String createTable : createTables) {
                    stmt.executeUpdate(createTable);
                }
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
                DataEntry next;
                while ((next = loader.nextEntry()) != null) {
                    insertStatement.setBytes(1, next.getKey());
                    insertStatement.setBytes(2, IOUtils.toByteArray(next.getValue()));
                    insertStatement.execute();
                }
            }

        }

    }

}
