package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;
import java.util.Objects;
import java.util.Optional;

public class CliResult {

  private final Integer status;

  private final boolean suppressStartup;

  private final Config config;

  public CliResult(Integer status, boolean suppressStartup, Config config) {
    this.status = Objects.requireNonNull(status);
    this.suppressStartup = suppressStartup;
    this.config = config;
  }

  public Integer getStatus() {
    return status;
  }

  public boolean isSuppressStartup() {
    return suppressStartup;
  }

  public Optional<Config> getConfig() {
    return Optional.ofNullable(config);
  }
}
