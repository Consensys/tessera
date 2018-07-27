package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.ServiceLoader;

public interface DataExporter {
    
    void export(Map<byte[], byte[]> data) throws SQLException,IOException;

    static DataExporter create() {
        return ServiceLoader.load(DataExporter.class).iterator().next();
    }
    
    default Path calculateExportPath() throws IOException {
        Path path =  Paths.get(System.getProperty("user.dir"))
                .resolve("target")
                .resolve(dbId())
                .resolve("tessera.db");
        Files.createDirectories(path.getParent());
        return path;
    }
    
    String dbId();
    
}
