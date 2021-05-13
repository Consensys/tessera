package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.ConfigFileStore;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import picocli.CommandLine;

public class ConfigConverter implements CommandLine.ITypeConverter<Config> {

  @Override
  public Config convert(final String value) throws Exception {
    final ConfigFactory configFactory = ConfigFactory.create();

    final Path path = Paths.get(value);

    if (!Files.exists(path)) {
      throw new FileNotFoundException(String.format("%s not found.", path));
    }

    ConfigFileStore.create(path);

    try (InputStream in = Files.newInputStream(path)) {
      return configFactory.create(in);
    }
  }
}
