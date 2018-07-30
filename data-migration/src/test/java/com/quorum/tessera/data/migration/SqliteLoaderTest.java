package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SqliteLoaderTest {

    private Path dbfilePath;

    private SqliteLoader loader;

    private Map<String, String> fixtures;

    //CREATE TABLE ENCRYPTED_TRANSACTION (ID BIGINT NOT NULL, ENCODED_PAYLOAD LONGVARBINARY NOT NULL, HASH LONGVARBINARY NOT NULL UNIQUE, PRIMARY KEY (ID))
    @Before
    public void doGenerateSample() throws Exception {
        fixtures = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            fixtures.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }

        dbfilePath = Files.createTempFile("sample", ".db");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfilePath);
                Statement statement = conn.createStatement();) {
            statement.execute("CREATE TABLE payload (key LONGVARBINARY,bytes LONGVARBINARY)");
            try (PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO payload (key,bytes) values (?,?)")) { 
                for (Entry<String,String> entry : fixtures.entrySet()) {
                    insertStatement.setString(1, entry.getKey());
                    insertStatement.setString(2,  entry.getValue());
                    insertStatement.executeUpdate();
                }

            }
        }

        loader = new SqliteLoader();
    }

    @After
    public void onTearDown() throws Exception {
        Files.deleteIfExists(dbfilePath);
    }

    @Test
    public void load() throws IOException {

       Map<byte[],byte[]> results =  loader.load(dbfilePath);

       assertThat(results).hasSize(fixtures.size());
       
       Map<String,String> resultz = results.entrySet().stream()
               .collect(Collectors.toMap(entry -> new String(entry.getKey()),entry -> new String(entry.getValue())));

       assertThat(resultz).containsAllEntriesOf(fixtures);
       
    }

}
