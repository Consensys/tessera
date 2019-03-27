package com.quorum.tessera.data.migration;

import java.net.URL;
import java.nio.file.Path;
import java.sql.*;
import java.util.Map;

public class SqliteDataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";


    @Override
    public void export(Map<byte[], byte[]> data, Path output, final String username, final String password) throws SQLException {

        final String connectionString = "jdbc:sqlite:" + output.toString();

        URL sqlFile = getClass().getResource("/ddls/h2-ddl.sql");

        JdbcDataExporter jdbcDataExporter = new JdbcDataExporter(connectionString,INSERT_ROW,sqlFile);

        jdbcDataExporter.export(data,output,username,password);

    }

}
