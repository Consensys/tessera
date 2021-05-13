package com.quorum.tessera.data.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.SQLiteException;

public class SqliteLoaderTest {

  private static final String INSERT_ROW = "INSERT INTO payload (key, bytes) values (?, ?)";

  private static final String CREATE_TABLE =
      "CREATE TABLE payload (key LONGVARBINARY, bytes LONGVARBINARY)";

  private Path dbfilePath;

  private SqliteLoader loader;

  private Map<String, String> fixtures;

  @Before
  public void doGenerateSample() throws Exception {
    this.fixtures = new LinkedHashMap<>();
    for (int i = 0; i < 10; i++) {
      this.fixtures.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    this.dbfilePath = Files.createTempFile("sample", ".db");
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfilePath);
        Statement statement = conn.createStatement()) {

      statement.execute(CREATE_TABLE);

      try (PreparedStatement insertStatement = conn.prepareStatement(INSERT_ROW)) {
        for (Entry<String, String> entry : fixtures.entrySet()) {
          insertStatement.setString(1, entry.getKey());
          insertStatement.setString(2, entry.getValue());
          insertStatement.executeUpdate();
        }
      }
    }

    this.loader = new SqliteLoader();
  }

  @Test
  public void loadOnInvalidFile() throws IOException {
    final Path randomFile = Files.createTempFile("other", ".txt");

    final Throwable throwable = catchThrowable(() -> loader.load(randomFile));
    assertThat(throwable)
        .isNotNull()
        .isInstanceOf(SQLiteException.class)
        .hasMessageContaining(
            "[SQLITE_ERROR] SQL error or missing database (no such table: payload)");
  }

  @Test
  public void loadOnNonexistentFile() throws IOException {
    final Path randomFile = Files.createTempFile("other", ".txt").getParent().resolve("unknown");

    final Throwable throwable = catchThrowable(() -> loader.load(randomFile));
    assertThat(throwable)
        .isNotNull()
        .isInstanceOf(SQLiteException.class)
        .hasMessageContaining(
            "[SQLITE_ERROR] SQL error or missing database (no such table: payload)");
  }

  @Test
  public void loadProperDatabaseHasNoError() {
    final Throwable throwable = catchThrowable(() -> loader.load(dbfilePath));

    assertThat(throwable).isNull();
  }

  @Test
  public void nextReturnsEntryWhenResultsAreLeft() throws SQLException {
    this.loader.load(dbfilePath);

    // There should be 10 results left in the database
    final DataEntry next = this.loader.nextEntry();

    assertThat(next).isNotNull();
  }

  @Test
  public void hasNextReturnsFalseWhenNoResultsAreLeft() throws SQLException {
    this.loader.load(dbfilePath);

    for (int i = 0; i < 10; i++) {
      this.loader.nextEntry();
    }

    // There should be 0 results left in the database
    final DataEntry next = this.loader.nextEntry();

    assertThat(next).isNull();
  }

  @Test
  public void dataIsReadCorrectly() throws SQLException, IOException {
    this.loader.load(dbfilePath);
    final Map<String, String> results = new HashMap<>();

    DataEntry next;
    while ((next = this.loader.nextEntry()) != null) {
      results.put(new String(next.getKey()), new String(IOUtils.toByteArray(next.getValue())));
    }

    assertThat(results).containsAllEntriesOf(fixtures);
  }
}
