package com.quorum.tessera.data.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    private static final String CREATE_TABLE_RESOURCE = "/ddls/h2-ddl.sql";

    @Override
    public void export(final StoreLoader loader, final Path output, final String username, final String password)
            throws SQLException, IOException {

        final String connectionString = "jdbc:h2:" + output.toString();

        final List<String> createTableStatements =
                Stream.of(getClass().getResourceAsStream(CREATE_TABLE_RESOURCE))
                        .map(InputStreamReader::new)
                        .map(BufferedReader::new)
                        .flatMap(BufferedReader::lines)
                        .collect(Collectors.toList());

        final JdbcDataExporter jdbcDataExporter =
                new JdbcDataExporter(connectionString, INSERT_ROW, createTableStatements);

        jdbcDataExporter.export(loader, output, username, password);
    }
}
