package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    private static final String CREATE_TABLE = "CREATE TABLE " +
        "ENCRYPTED_TRANSACTION (" +
        "ENCODED_PAYLOAD LONGVARBINARY NOT NULL, " +
        "HASH LONGVARBINARY NOT NULL, " +
        "TIMESTAMP BIGINT, " +
        "PRIMARY KEY (HASH)" +
        ")";

    @Override
    public void export(final Map<byte[], byte[]> data,
                       final Path output,
                       final String username,
                       final String password) throws SQLException {

        final String connectionString = "jdbc:h2:" + output.toString();

        JdbcDataExporter jdbcDataExporter = new JdbcDataExporter(connectionString,INSERT_ROW,CREATE_TABLE);

        jdbcDataExporter.export(data,output,username,password);

    }

}
