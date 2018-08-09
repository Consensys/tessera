package com.quorum.tessera.data.migration;

import java.io.File;

import org.h2.jdbc.JdbcSQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Rule;
import org.junit.rules.TestName;

public class H2DataExporterTest {

    private H2DataExporter exporter;

    private Path outputPath;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void onSetUp() throws IOException {

        exporter = new H2DataExporter();
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

        Path outputpath = Files.createTempFile("exportSingleLine", ".db");

        Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData, outputpath, null, null);

        String connectionString = "jdbc:h2:" + outputpath;

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM ENCRYPTED_TRANSACTION").executeQuery()) {
                while (rs.next()) {
                    assertThat(rs.getBytes("HASH")).isEqualTo("HASH".getBytes());
                    assertThat(rs.getBytes("ENCODED_PAYLOAD")).isEqualTo("VALUE".getBytes());
                }

            }

        }

    }

    @Test
    public void exportSingleLineWithUsernameAndPassword() throws SQLException, IOException {

        final String username = "sa";
        final String password = "pass";

        final Path outputpath = Files.createTempFile("exportSingleLine", ".db");

        final Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData, outputpath, username, password);

        final String connectionString = "jdbc:h2:" + outputpath;

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            try (ResultSet rs = conn.prepareStatement("SELECT * FROM ENCRYPTED_TRANSACTION").executeQuery()) {
                while (rs.next()) {
                    assertThat(rs.getBytes("HASH")).isEqualTo("HASH".getBytes());
                    assertThat(rs.getBytes("ENCODED_PAYLOAD")).isEqualTo("VALUE".getBytes());
                }

            }

        }

    }

    @Test
    public void exportSingleLineWithUsernameAndPasswordFailsWhenReading() throws SQLException, IOException {

        final String username = "sa";
        final String password = "pass";

        final Path outputpath = Files.createTempFile("exportSingleLine", ".db");

        final Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData, outputpath, username, password);

        final String connectionString = "jdbc:h2:" + outputpath;

        final Throwable throwable = catchThrowable(() -> DriverManager.getConnection(connectionString, null, null));

        assertThat(throwable).isInstanceOf(JdbcSQLException.class);

    }

}
