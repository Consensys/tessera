package com.quorum.tessera.data.migration;

import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public interface DataExporter {

    void export(Map<byte[], InputStream> data, Path output, String username, String password) throws SQLException;

}
