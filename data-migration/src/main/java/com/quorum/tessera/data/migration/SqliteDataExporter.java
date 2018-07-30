package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

//FIXME: Need to address sequence generation config for sql bfeore going live with this
public class SqliteDataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data, Path output) throws SQLException, IOException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        try (Connection conn = DriverManager.getConnection(connectionString)) {

            try (Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("CREATE TABLE ENCRYPTED_TRANSACTION "
                        + "(ENCODED_PAYLOAD LONGVARBINARY NOT NULL, "
                        + "HASH LONGVARBINARY NOT NULL UNIQUE, "
                        + "PRIMARY KEY (HASH))");


            }

            try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO ENCRYPTED_TRANSACTION "
                    + "(HASH,ENCODED_PAYLOAD) "
                    + "VALUES (?,?)")) {
                for (Map.Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }

}
