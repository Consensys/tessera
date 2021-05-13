package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public interface DataExporter {

  void export(StoreLoader loader, Path output, String username, String password)
      throws IOException, SQLException;
}
