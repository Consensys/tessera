package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.io.UriCallback;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    @Override
    public void export(Map<byte[], InputStream> data, Path output, String username, String password) throws SQLException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        final URI sqlFile = UriCallback.execute(() -> getClass().getResource("/ddls/sqlite-ddl.sql").toURI());

        List<String> createTables = IOCallback.execute(() -> Files.readAllLines(Paths.get(sqlFile)));

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {

            try (Statement stmt = conn.createStatement()) {
                for (String createTable : createTables) {
                    stmt.executeUpdate(createTable);
                }
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
                for (Map.Entry<byte[], InputStream> values : data.entrySet()) {
                    insertStatement.setBytes(1, values.getKey());

                    insertStatement.setBytes(2, Utils.toByteArray(values.getValue()));

                    insertStatement.execute();
                }
            }

        }
    }


}
