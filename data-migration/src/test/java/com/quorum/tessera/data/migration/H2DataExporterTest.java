package com.quorum.tessera.data.migration;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.h2.jdbc.JdbcSQLInvalidAuthorizationSpecException;
import org.junit.Before;
import org.junit.Test;

public class H2DataExporterTest {

  private static final String QUERY = "SELECT * FROM ENCRYPTED_TRANSACTION";

  private H2DataExporter exporter;

  @Before
  public void onSetUp() {
    this.exporter = new H2DataExporter();
  }

  @Test
  public void exportSingleLine() throws SQLException, IOException {

    final Path outputPath = Files.createTempFile("exportSingleLine", ".db");

    final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

    exporter.export(mockLoader, outputPath, null, null);

    final String connectionString = "jdbc:h2:" + outputPath;

    try (Connection conn = DriverManager.getConnection(connectionString);
        ResultSet rs = conn.prepareStatement(QUERY).executeQuery()) {

      final ResultSetMetaData metaData = rs.getMetaData();
      final List<String> columnNames =
          IntStream.range(1, metaData.getColumnCount() + 1)
              .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
              .collect(Collectors.toList());

      assertThat(columnNames).containsExactlyInAnyOrder("HASH", "ENCODED_PAYLOAD", "TIMESTAMP");

      while (rs.next()) {
        assertThat(rs.getBytes("HASH")).isEqualTo("HASH".getBytes());
        assertThat(rs.getBytes("ENCODED_PAYLOAD")).isEqualTo("VALUE".getBytes());
      }
    }
  }

  @Test
  public void exportSingleLineWithUsernameAndPassword() throws SQLException, IOException {

    final String username = "sa";
    final String password = "pass";

    final Path outputPath = Files.createTempFile("exportSingleLine", ".db");

    final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

    exporter.export(mockLoader, outputPath, username, password);

    final String connectionString = "jdbc:h2:" + outputPath;

    try (Connection conn = DriverManager.getConnection(connectionString, username, password);
        ResultSet rs = conn.prepareStatement(QUERY).executeQuery()) {

      final ResultSetMetaData metaData = rs.getMetaData();
      final List<String> columnNames =
          IntStream.range(1, metaData.getColumnCount() + 1)
              .mapToObj(i -> JdbcCallback.execute(() -> metaData.getColumnName(i)))
              .collect(Collectors.toList());

      assertThat(columnNames).containsExactlyInAnyOrder("HASH", "ENCODED_PAYLOAD", "TIMESTAMP");

      while (rs.next()) {
        assertThat(rs.getBytes("HASH")).isEqualTo("HASH".getBytes());
        assertThat(rs.getBytes("ENCODED_PAYLOAD")).isEqualTo("VALUE".getBytes());
      }
    }
  }

  @Test
  public void exportSingleLineWithUsernameAndPasswordFailsWhenReading()
      throws SQLException, IOException {
    final String username = "sa";
    final String password = "pass";

    final Path outputPath = Files.createTempFile("exportSingleLine", ".db");

    final StoreLoader mockLoader = new MockDataLoader(singletonMap("HASH", "VALUE"));

    exporter.export(mockLoader, outputPath, username, password);

    final String connectionString = "jdbc:h2:" + outputPath;

    final Throwable throwable =
        catchThrowable(() -> DriverManager.getConnection(connectionString, null, null));

    assertThat(throwable).isInstanceOf(JdbcSQLInvalidAuthorizationSpecException.class);
  }
}
