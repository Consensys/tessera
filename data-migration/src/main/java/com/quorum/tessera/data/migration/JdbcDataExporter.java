package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class JdbcDataExporter implements DataExporter {

  private final String jdbcUrl;

  private final String insertRow;

  private final List<String> createTables;

  public JdbcDataExporter(
      final String jdbcUrl, final String insertRow, final List<String> createTables) {
    this.jdbcUrl = jdbcUrl;
    this.insertRow = insertRow;
    this.createTables = createTables;
  }

  @Override
  public void export(
      final StoreLoader loader, final Path output, final String username, final String password)
      throws SQLException, IOException {

    try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

      try (Statement stmt = conn.createStatement()) {
        for (final String createTable : createTables) {
          stmt.executeUpdate(createTable);
        }
      }

      try (PreparedStatement insertStatement = conn.prepareStatement(insertRow)) {
        DataEntry next;
        while ((next = loader.nextEntry()) != null) {
          try (InputStream data = next.getValue()) {
            insertStatement.setBytes(1, next.getKey());
            insertStatement.setBinaryStream(2, data);
            insertStatement.execute();
          }
        }
      }
    }
  }
}
