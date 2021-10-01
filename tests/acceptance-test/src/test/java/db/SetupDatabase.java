package db;

import com.quorum.tessera.config.Config;
import config.ConfigDescriptor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.ExecutionContext;
import suite.NodeAlias;

public class SetupDatabase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SetupDatabase.class);

  private ExecutionContext executionContext;

  public SetupDatabase(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public void setUp(NodeAlias nodeAlias) throws Exception {
    URL ddl = executionContext.getDbType().getDdl();
    List<String> lines = Files.readAllLines(Paths.get(ddl.toURI()));
    try (Connection connection = getConnection(nodeAlias)) {
      try (Statement statement = connection.createStatement()) {
        for (String line : lines) {
          LOGGER.trace("Create table SQL : {}", line);
          statement.execute(line);
        }
      }
    }
  }

  public void setUp() throws Exception {

    URL ddl = executionContext.getDbType().getDdl();

    List<Connection> connections = getConnections();

    List<String> lines = Files.readAllLines(Paths.get(ddl.toURI()));

    for (Connection connection : connections) {

      try (Statement statement = connection.createStatement()) {
        for (String line : lines) {
          LOGGER.trace("Create table SQL : {}", line);
          statement.execute(line);
        }
      }
    }

    for (Connection connection : connections) {
      try {
        connection.close();
      } catch (SQLException ex) {
      }
    }
  }

  public Connection getConnection(NodeAlias nodeAlias) {
    return executionContext.getConfigs().stream()
        .filter(c -> c.getAlias() == nodeAlias)
        .map(ConfigDescriptor::getConfig)
        .map(Config::getJdbcConfig)
        .map(
            j -> {
              try {
                LOGGER.info("{}", j.getUrl());
                return DriverManager.getConnection(j.getUrl(), j.getUsername(), j.getPassword());
              } catch (SQLException ex) {
                throw new UncheckedSQLException(ex);
              }
            })
        .findFirst()
        .get();
  }

  public List<Connection> getConnections() {
    return Arrays.stream(NodeAlias.values()).map(this::getConnection).collect(Collectors.toList());
  }

  public void drop(NodeAlias nodeAlias) throws SQLException {

    try (Connection connection = getConnection(nodeAlias)) {
      DatabaseMetaData metaData = connection.getMetaData();
      if (Objects.isNull(metaData)) {
        LOGGER.warn("No connection metadata returning");
        return;
      }
      List<String> tableNames = new ArrayList<>();
      try (ResultSet rs = metaData.getTables(null, null, "%", null)) {

        while (rs.next()) {
          tableNames.add(rs.getString(3));
        }
      }

      String dropStatement = "DROP TABLE %s";

      try (Statement statement = connection.createStatement()) {
        for (String tableName : tableNames) {
          String line = String.format(dropStatement, tableName);
          LOGGER.trace("Drop table SQL : {}", line);
          try {
            statement.execute(line);
          } catch (SQLException ex) {
          }
        }
      }
    }
  }

  public void dropAll() throws Exception {

    List<Connection> connections = getConnections();
    for (Connection connection : connections) {
      DatabaseMetaData metaData = connection.getMetaData();
      List<String> tableNames = new ArrayList<>();
      try (ResultSet rs = metaData.getTables(null, null, "%", null)) {

        while (rs.next()) {
          tableNames.add(rs.getString(3));
        }
      }

      String dropStatement = "DROP TABLE %s";

      try (Statement statement = connection.createStatement()) {
        for (String tableName : tableNames) {
          String line = String.format(dropStatement, tableName);
          LOGGER.trace("Drop table SQL : {}", line);
          try {
            statement.execute(line);
          } catch (SQLException ex) {
          }
        }
      }
    }

    for (Connection connection : connections) {
      try {
        connection.close();
      } catch (SQLException ex) {
      }
    }
  }
}
