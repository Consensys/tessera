package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public interface DataExporter {

    void export(Map<byte[], byte[]> data, Path output, String username, String password) throws SQLException;

}
