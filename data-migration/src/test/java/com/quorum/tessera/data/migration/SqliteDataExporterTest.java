
package com.quorum.tessera.data.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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


public class SqliteDataExporterTest {
    
    private SqliteDataExporter exporter;

    @Before
    public void onSetUp() throws IOException {

        exporter = new SqliteDataExporter();

    }

    @After
    public void onTearDown() throws IOException {
        Files.walk(exporter.calculateExportPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
    }

    @Test
    public void exportSingleLine() throws SQLException, IOException {

        Map<byte[], byte[]> singleLineData = new HashMap<>();
        singleLineData.put("HASH".getBytes(), "VALUE".getBytes());

        exporter.export(singleLineData);

        String connectionString = "jdbc:sqlite:" + exporter.calculateExportPath();

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ENCRYPTED_TRANSACTION")) {
                while (rs.next()) {
                    assertThat(rs.getString("HASH")).isEqualTo("HASH");
                    assertThat(rs.getString("ENCODED_PAYLOAD")).isEqualTo("VALUE");
                }

            }

        }

    }
}
