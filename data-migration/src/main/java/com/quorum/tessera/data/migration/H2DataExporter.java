package com.quorum.tessera.data.migration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    private static final String CREATE_TABLE_RESOURCE = "/ddls/h2-ddl.sql";

    @Override
    public void export(final StoreLoader loader,
                       final Path output,
                       final String username,
                       final String password) throws SQLException, IOException {

        final String connectionString = "jdbc:h2:" + output.toString();

        final byte[] data = IOUtils.resourceToByteArray(CREATE_TABLE_RESOURCE);
        final String dataAsString = new String(data, UTF_8);
        final List<String> createTableStatements = Arrays.asList(dataAsString.split("\n"));

        final JdbcDataExporter jdbcDataExporter
            = new JdbcDataExporter(connectionString, INSERT_ROW, createTableStatements);

        jdbcDataExporter.export(loader, output, username, password);
    }

}
