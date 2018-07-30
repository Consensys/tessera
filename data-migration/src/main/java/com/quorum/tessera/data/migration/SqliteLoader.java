package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SqliteLoader implements StoreLoader {

    @Override
    public Map<byte[], byte[]> load(Path input) throws IOException {

        final String url = "jdbc:sqlite:" + input.toString();

        return JdbcCallback.execute(() -> {

            try (Connection conn = DriverManager.getConnection(url);
                    Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery("SELECT * FROM payload")) {

                Map<byte[], byte[]> loadedData = new HashMap<>();
                while (results.next()) {

                    byte[] key = results.getBytes("key");
                    byte[] value = results.getBytes("bytes");

                    loadedData.put(key, value);

                }

                return Collections.unmodifiableMap(loadedData);

            }
        });

    }

}
