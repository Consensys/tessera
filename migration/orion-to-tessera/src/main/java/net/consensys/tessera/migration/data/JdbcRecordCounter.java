package net.consensys.tessera.migration.data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcRecordCounter implements RecordCounter {

    private final DataSource dataSource;

    public JdbcRecordCounter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long count() throws Exception {

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM STORE");

        try (connection;
                statement;
                resultSet) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
