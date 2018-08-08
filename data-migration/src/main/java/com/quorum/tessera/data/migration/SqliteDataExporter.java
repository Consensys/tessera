package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.*;
import java.util.Map;

//FIXME: Need to address sequence generation config for sql bfeore going live with this
public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    private static final String CREATE_TABLE = "CREATE TABLE ENCRYPTED_TRANSACTION "
        + "(ENCODED_PAYLOAD LONGVARBINARY NOT NULL, "
        + "HASH LONGVARBINARY NOT NULL UNIQUE, PRIMARY KEY (HASH))";

    @Override
    public void export(Map<byte[], byte[]> data, Path output, final String username, final String password) throws SQLException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(CREATE_TABLE);
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
                for (Map.Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

}
