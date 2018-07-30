package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;

public class H2DataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data,Path output) throws SQLException,IOException {

        final String connectionString = "jdbc:h2:"+ output.toString();
        
        try (Connection conn = DriverManager.getConnection(connectionString)) {

            try (Statement stmt = conn.createStatement()) {
                
                stmt.executeUpdate("CREATE TABLE ENCRYPTED_TRANSACTION "
                        + "(ENCODED_PAYLOAD LONGVARBINARY NOT NULL, "
                        + "HASH LONGVARBINARY NOT NULL UNIQUE, PRIMARY KEY (HASH))");
                }

            try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)")) {
                for (Entry<byte[], byte[]> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());
                    insertStatement.setBytes(2, values.getValue());
                    insertStatement.execute();
                }
            }

        }
    }


}
