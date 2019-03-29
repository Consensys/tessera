package com.quorum.tessera.data.migration;

import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH,ENCODED_PAYLOAD) VALUES (?,?)";

    @Override
    public void export(final Map<byte[], byte[]> data,
                       final Path output,
                       final String username,
                       final String password) throws SQLException {

        final String connectionString = "jdbc:h2:" + output.toString();

        final URL sqlFile = getClass().getResource("/ddls/h2-ddl.sql");

        final JdbcDataExporter jdbcDataExporter = new JdbcDataExporter(connectionString, INSERT_ROW, sqlFile);

        jdbcDataExporter.export(data, output, username, password);

    }

}
