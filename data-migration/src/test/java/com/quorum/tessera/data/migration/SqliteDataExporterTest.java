package com.quorum.tessera.data.migration;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class SqliteDataExporterTest {

    private static final String QUERY = "SELECT * FROM ENCRYPTED_TRANSACTION";

    private SqliteDataExporter exporter;

    @Before
    public void onSetUp() {
        this.exporter = new SqliteDataExporter();
    }

    @Test
    public void exportSingleLine() throws SQLException, IOException {

        final Path outputPath = Files.createTempFile("exportSingleLine", ".db");

        final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

        exporter.export(mockLoader, outputPath, null, null);

        final String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString);
             ResultSet rs = conn.createStatement().executeQuery(QUERY)) {

            final ResultSetMetaData metaData = rs.getMetaData();
            final List<String> columnNames = IntStream
                .range(1, metaData.getColumnCount() + 1)
                .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
                .collect(Collectors.toList());

            assertThat(columnNames).containsExactlyInAnyOrder("HASH", "ENCODED_PAYLOAD", "TIMESTAMP");

            while (rs.next()) {
                assertThat(rs.getString("TIMESTAMP")).isNull();
                assertThat(rs.getString("HASH")).isEqualTo("HASH");
                assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
            }

        }

    }

    @Test
    public void exportSingleLineWithUsernameAndPassword() throws SQLException, IOException {

        final Path outputPath = Files.createTempFile("exportSingleLine", ".db");

        final String username = "sa";
        final String password = "pass";

        final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

        exporter.export(mockLoader, outputPath, username, password);

        final String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString, username, password);
             ResultSet rs = conn.createStatement().executeQuery(QUERY)) {

            final ResultSetMetaData metaData = rs.getMetaData();
            final List<String> columnNames = IntStream
                .range(1, metaData.getColumnCount() + 1)
                .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
                .collect(Collectors.toList());

            assertThat(columnNames).containsExactlyInAnyOrder("HASH", "ENCODED_PAYLOAD", "TIMESTAMP");

            while (rs.next()) {
                assertThat(rs.getString("TIMESTAMP")).isNull();
                assertThat(rs.getString("HASH")).isEqualTo("HASH");
                assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
            }

        }

    }

}
