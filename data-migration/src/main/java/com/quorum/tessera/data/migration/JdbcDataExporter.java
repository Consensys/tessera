package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.io.UriCallback;
import java.io.InputStream;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JdbcDataExporter implements DataExporter {

    private final String jdbcUrl;

    private final String insertRow;

    private final List<String> createTables;

    public JdbcDataExporter(String jdbcUrl, String insertRow, URL ddl) {
        final Path uri = UriCallback.execute(() -> Paths.get(ddl.toURI()));

        this.jdbcUrl = jdbcUrl;
        this.insertRow = insertRow;
        this.createTables = IOCallback.execute(() -> Files.readAllLines(uri));
    }

    @Override
    public void export(Map<byte[], InputStream> data, Path output, String username, String password) throws SQLException {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                for (String createTable : createTables) {
                    stmt.executeUpdate(createTable);
                }
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(insertRow)) {
                for (Entry<byte[], InputStream> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBinaryStream(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

}
