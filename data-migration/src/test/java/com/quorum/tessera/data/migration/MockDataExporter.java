package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.util.Map;

public class MockDataExporter implements DataExporter {

    private Map<byte[], byte[]> results;

    private Path path;

    private String username;

    private String password;

    @Override
    public void export(Map<byte[], byte[]> data, Path path, String username, String password) {
        this.results = data;
        this.path = path;
        this.username = username;
        this.password = password;
    }

    public Map<byte[], byte[]> getResults() {
        return results;
    }

    public Path getPath() {
        return path;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
