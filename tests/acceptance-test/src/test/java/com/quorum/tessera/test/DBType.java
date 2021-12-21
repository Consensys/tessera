package com.quorum.tessera.test;

import db.DatabaseServer;
import db.HsqlDatabaseServer;
import java.net.URL;

public enum DBType {
  H2(
      "jdbc:h2:./build/h2/%s%d;TRACE_LEVEL_SYSTEM_OUT=0;AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE",
      "/ddls/h2-ddl.sql"),
  HSQL("jdbc:hsqldb:hsql://127.0.0.1:9189/%s%d", "/ddls/hsql-ddl.sql"),
  SQLITE("jdbc:sqlite:build/sqlite-%s%d.db", "/ddls/sqlite-ddl.sql");

  private final String urlTemplate;

  private URL ddl;

  DBType(String urlTemplate, String ddl) {
    this.urlTemplate = urlTemplate;
    this.ddl = getClass().getResource(ddl);
  }

  public String createUrl(String nodeId, int nodeNumber) {
    return String.format(urlTemplate, nodeId, nodeNumber);
  }

  public URL getDdl() {
    return ddl;
  }

  public DatabaseServer createDatabaseServer(String nodeId) {

    if (this == HSQL) {
      return new HsqlDatabaseServer(nodeId);
    }

    return new DatabaseServer() {};
  }
}
