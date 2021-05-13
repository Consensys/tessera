package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.io.IOCallback;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public interface ConfigFileStore {

  enum Store implements ConfigFileStore {
    INSTANCE;

    private Path path;

    @Override
    public void save(Config config) {

      IOCallback.execute(
          () -> {
            Path temp = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (OutputStream fout = Files.newOutputStream(temp)) {
              JaxbUtil.marshalWithNoValidation(config, fout);
            }
            Files.copy(temp, path, StandardCopyOption.REPLACE_EXISTING);
            return null;
          });
    }
  }

  static ConfigFileStore create(Path path) {
    Store.INSTANCE.path = path;
    return Store.INSTANCE;
  }

  static ConfigFileStore get() {
    return Store.INSTANCE;
  }

  void save(Config config);
}
