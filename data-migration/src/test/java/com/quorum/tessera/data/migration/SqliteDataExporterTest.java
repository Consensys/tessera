package com.quorum.tessera.data.migration;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;


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

        Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData, outputPath, null, null);

        String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ENCRYPTED_TRANSACTION")) {
                while (rs.next()) {
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

        final  Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData, outputPath, username, password);

        String connectionString = "jdbc:sqlite:" + outputPath;

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ENCRYPTED_TRANSACTION")) {
                while (rs.next()) {
                    assertThat(rs.getString("HASH")).isEqualTo("HASH");
                    assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
                }

            }

        }

    }

}
