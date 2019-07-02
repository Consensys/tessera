package com.quorum.tessera.data.migration;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.sql.*;

public class SqliteLoader implements StoreLoader {

    private static final String SELECT_QUERY = "SELECT * FROM payload";

    private Connection connection;

    private Statement statement;

    private ResultSet results;

    @Override
    public void load(final Path input) throws SQLException {
        final String url = "jdbc:sqlite:" + input.toString();

        this.connection = DriverManager.getConnection(url);
        this.statement = this.connection.createStatement();
        this.results = this.statement.executeQuery(SELECT_QUERY);
    }

    @Override
    public DataEntry nextEntry() throws SQLException {
        final boolean hasNextEntry = this.results.next();
        if (!hasNextEntry) {
            this.results.close();
            this.statement.close();
            this.connection.close();
            return null;
        }

        return new DataEntry(results.getBytes("key"), new ByteArrayInputStream(results.getBytes("bytes")));
    }
}
