package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import java.nio.file.Path;
import picocli.CommandLine;

public class ServerURIOutputMixin {
  @CommandLine.Option(
      names = {"--XoutputServerURIPath"},
      description = "Output the server URI(s) to a specified path",
      hidden = true)
  private Path outputServerURIPath = null;

  public void updateConfig(final Config config) {
    updateConfig(this.outputServerURIPath, config);
  }

  public void updateConfig(final Path outputServerURIPath, final Config config) {
    if (outputServerURIPath != null) {
      config.setOutputServerURIPath(outputServerURIPath);
    }
  }
}
