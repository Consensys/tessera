package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.io.UriCallback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class H2DataExporter implements DataExporter {

    private static final String INSERT_ROW = "INSERT INTO ENCRYPTED_TRANSACTION (HASH, ENCODED_PAYLOAD) VALUES (?, ?)";

    private static final String CREATE_TABLE_RESOURCE = "/ddls/h2-ddl.sql";

    @Override
    public void export(final StoreLoader loader,
                       final Path output,
                       final String username,
                       final String password) throws SQLException, IOException {

        final String connectionString = "jdbc:h2:" + output.toString();

        final URL sqlFile = getClass().getResource(CREATE_TABLE_RESOURCE);
        final Path uri = UriCallback.execute(() -> Paths.get(sqlFile.toURI()));
        final List<String> createTableStatements = IOCallback.execute(() -> Files.readAllLines(uri));

        final JdbcDataExporter jdbcDataExporter
            = new JdbcDataExporter(connectionString, INSERT_ROW, createTableStatements);

        jdbcDataExporter.export(loader, output, username, password);
    }

}
