package net.consensys.tessera.migration.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Map;
import javax.sql.DataSource;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

public interface LevelDbToJdbcUtil {

  static void copy(DB leveldb, DataSource dataSource) throws Exception {

    try (Connection connection = dataSource.getConnection()) {
      connection
          .createStatement()
          .execute(
              "CREATE TABLE STORE (\n" + "  KEY CHAR(60) PRIMARY KEY,\n" + "  VALUE BLOB\n" + ")");
      connection.commit();
    }

    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement("INSERT INTO STORE (KEY,VALUE) VALUES (?,?)")) {

      DBIterator iterator = leveldb.iterator();
      for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
        Map.Entry<byte[], byte[]> entry = iterator.peekNext();

        byte[] key = entry.getKey();
        byte[] value = entry.getValue();

        statement.setString(1, Base64.getEncoder().encodeToString(key));
        statement.setBytes(2, value);

        statement.execute();
      }

      statement.executeBatch();
    }
  }
}
