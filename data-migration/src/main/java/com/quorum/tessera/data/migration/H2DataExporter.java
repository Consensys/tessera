package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    private static final String CREATE_TABLE = "CREATE TABLE ENCRYPTED_TRANSACTION "
        + "(ENCODED_PAYLOAD LONGVARBINARY NOT NULL, "
        + "HASH LONGVARBINARY NOT NULL UNIQUE, PRIMARY KEY (HASH))";

    @Override
    public void export(final Map<byte[], byte[]> data,
                       final Path output,
                       final String username,
                       final String password) throws SQLException {

        final String connectionString = "jdbc:h2:" + output.toString();

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(CREATE_TABLE);
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
                for (Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

}
