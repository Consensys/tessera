package com.quorum.tessera.config.cli;

import picocli.CommandLine;

public class DebugOptions {
  // this is currently only used for help usage
  @CommandLine.Option(
      names = {"--debug"},
      description = "Print full exception stack traces")
  public boolean isDebugEnabled;
}
