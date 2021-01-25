package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;

import javax.json.JsonObject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

public class JdbcOrionDataAdapter implements OrionDataAdapter {

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final DataSource dataSource;

    private Long totalRecords;

    public JdbcOrionDataAdapter(DataSource dataSource,
                                ObjectMapper cborObjectMapper,
                                Disruptor<OrionEvent> disruptor) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
    }

    @Override
    public void start() throws Exception {

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM STORE");

        if(Objects.isNull(totalRecords)) {
            try(ResultSet countRs = statement.executeQuery("SELECT COUNT(*) FROM STORE")) {
                countRs.next();
                totalRecords = countRs.getLong(1);
            }
        }

        try (connection;
                statement;
                resultSet) {

            for (long i = 1;resultSet.next();i++) {
                byte[] key = resultSet.getBytes("KEY");
                byte[] value = resultSet.getBytes("VALUE");

                JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);
                PayloadType payloadType = PayloadType.get(jsonObject);

                disruptor.publishEvent(new OrionEvent(payloadType, jsonObject, key, value,totalRecords,i));

            }
        }
    }
}
