package net.consensys.tessera.migration.data;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public interface LevelDbToJdbcUtil {

    static void copy(DB leveldb, DataSource dataSource) throws Exception {

        try(Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE STORE (\n" +
                "  key char(60) primary KEY,\n" +
                "  VALUE blob\n" +
                ");");
            connection.commit();
        }

        int batchCount = 0;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO STORE (KEY,VALUE) VALUES (?,?)")) {

            DBIterator iterator = leveldb.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                Map.Entry<byte[], byte[]> entry = iterator.peekNext();

                byte[] key = entry.getKey();
                byte[] value = entry.getValue();

                PayloadType payloadType = PayloadType.parsePayloadType(value);
                System.out.println(payloadType + " "+ new String(key) + " "+ key.length);

                statement.setString(1,new String(key));
                statement.setBytes(2,value);

                if(batchCount > 100) {
                    statement.executeBatch();
                    batchCount = 0;
                    continue;
                }
                batchCount++;
            }

            statement.executeBatch();
        }
    }

}
