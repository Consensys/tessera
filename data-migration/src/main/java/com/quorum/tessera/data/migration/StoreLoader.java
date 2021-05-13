package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public interface StoreLoader {

  void load(Path input) throws IOException, SQLException;

  DataEntry nextEntry() throws IOException, SQLException;
}
