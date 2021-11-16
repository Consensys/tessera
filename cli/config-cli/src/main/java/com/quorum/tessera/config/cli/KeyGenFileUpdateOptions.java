package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import java.nio.file.Path;
import picocli.CommandLine;

public class KeyGenFileUpdateOptions {
  @CommandLine.Option(
      names = {"--configfile", "-configfile", "--config-file"},
      description = "Path to node configuration file",
      required = true)
  private Config config;

  @CommandLine.Option(
      names = {"--configout", "-output"},
      description =
          "Path to save updated configfile to.  Requires --configfile option to also be provided")
  private Path configOut;

  @CommandLine.Option(
      names = {"--pwdout"},
      description =
          "Path to save updated password list to.  Requires --configfile and --configout options to also be provided")
  private Path pwdOut;

  public Config getConfig() {
    return config;
  }

  public Path getConfigOut() {
    return configOut;
  }

  public Path getPwdOut() {
    return pwdOut;
  }
}
