package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;

public class JdbcDataExporter implements DataExporter {

    private final String jdbcUrl;

    private final String insertRow;

    private final String createTable;

    public JdbcDataExporter(String jdbcUrl, String insertRow, String createTable) {
        this.jdbcUrl = jdbcUrl;
        this.insertRow = insertRow;
        this.createTable = createTable;
    }

    @Override
    public void export(Map<byte[], byte[]> data, Path output, String username, String password) throws SQLException {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createTable);
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(insertRow)) {
                for (Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

}
