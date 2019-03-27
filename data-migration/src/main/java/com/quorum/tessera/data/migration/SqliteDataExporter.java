package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.*;
import java.util.Map;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    private static final String CREATE_TABLE = "CREATE TABLE ENCRYPTED_TRANSACTION (ENCODED_PAYLOAD BLOB NOT NULL, TIMESTAMP NUMBER(19), HASH BLOB NOT NULL, PRIMARY KEY (HASH))";

    @Override
    public void export(Map<byte[], byte[]> data, Path output, final String username, final String password) throws SQLException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        JdbcDataExporter jdbcDataExporter = new JdbcDataExporter(connectionString,INSERT_ROW,CREATE_TABLE);

        jdbcDataExporter.export(data,output,username,password);

    }

}
