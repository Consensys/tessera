package db;

import java.io.IOException;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl;

public class HsqlDatabaseServer implements DatabaseServer {

  private final Server hsqlServer = new Server();

  private final String nodeId;

  public HsqlDatabaseServer(String nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public void start() {

    HsqlProperties properties = new HsqlProperties();
    for (int i = 0; i < 4; i++) {
      String db = nodeId + (i + 1);
      properties.setProperty(
          "server.database." + i, "file:build/hsql/" + db + ";user=sa;password=password");
      properties.setProperty("server.dbname." + i, db);
    }

    properties.setProperty("server.database.4", "file:build/hsql/rest-httpwhitelist5");
    properties.setProperty("server.dbname.4", "rest-httpwhitelist5");

    hsqlServer.setPort(9189);
    hsqlServer.setSilent(true);
    hsqlServer.setTrace(false);
    try {
      hsqlServer.setProperties(properties);
    } catch (IOException | ServerAcl.AclFormatException ex) {
      throw new RuntimeException(ex);
    }
    hsqlServer.start();
    if (hsqlServer.isNotRunning()) {
      throw new IllegalStateException("HSQL DB not started. ");
    }
  }

  @Override
  public void stop() {
    hsqlServer.shutdown();
  }
}
