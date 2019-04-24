package com.quorum.tessera.data.migration;

import java.io.ByteArrayInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SqliteDataExporterTest {

    private SqliteDataExporter exporter;

    private Path outputPath;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void onSetUp() throws IOException {
        exporter = new SqliteDataExporter();
        outputPath = Files.createTempFile(testName.getMethodName(), ".db");

    }

    @After
    public void onTearDown() throws IOException {
        Files.walk(outputPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void exportSingleLine() throws SQLException, IOException {

        Map<byte[], InputStream> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), new ByteArrayInputStream("VALUE".getBytes()));

        exporter.export(singleLineData, outputPath, null, null);

        String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ENCRYPTED_TRANSACTION")) {

                ResultSetMetaData metaData = rs.getMetaData();
                List<String> columnNames = IntStream.range(1,metaData.getColumnCount() + 1)
                    .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
                    .collect(Collectors.toList());

                assertThat(columnNames).containsExactlyInAnyOrder("HASH","ENCODED_PAYLOAD","TIMESTAMP");

                while (rs.next()) {
                    assertThat(rs.getString("TIMESTAMP")).isNull();
                    assertThat(rs.getString("HASH")).isEqualTo("HASH");
                    assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
                }

            }

        }

    }

    @Test
    public void exportSingleLineWithUsernameAndPassword() throws SQLException {

        final String username = "sa";
        final String password = "pass";

        final Map<byte[], InputStream> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), new ByteArrayInputStream("VALUE".getBytes()));

        exporter.export(singleLineData, outputPath, username, password);

        String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ENCRYPTED_TRANSACTION")) {

                ResultSetMetaData metaData = rs.getMetaData();
                List<String> columnNames = IntStream.range(1,metaData.getColumnCount() + 1)
                    .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
                    .collect(Collectors.toList());

                assertThat(columnNames).containsExactlyInAnyOrder("HASH","ENCODED_PAYLOAD","TIMESTAMP");

                while (rs.next()) {
                    assertThat(rs.getString("TIMESTAMP")).isNull();
                    assertThat(rs.getString("HASH")).isEqualTo("HASH");
                    assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
                }

            }

        }

    }

}
