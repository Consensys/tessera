package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import net.consensys.tessera.migration.OrionKeyHelper;

import javax.json.JsonObject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

public class JdbcOrionDataAdapter implements OrionDataAdapter {

    private final ObjectMapper cborObjectMapper;

    private final OrionKeyHelper orionKeyHelper;

    private Disruptor<OrionRecordEvent> disruptor;

    private DataSource dataSource;

    public JdbcOrionDataAdapter(DataSource dataSource, ObjectMapper cborObjectMapper, OrionKeyHelper orionKeyHelper) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
    }

    @Override
    public void start() throws Exception {

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM STORE");
        try (connection;
                statement;
                resultSet) {

            while (resultSet.next()) {
                String key = resultSet.getString("KEY");
                byte[] value = resultSet.getBytes("VALUE");

                JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);
            }
        }
    }
}
