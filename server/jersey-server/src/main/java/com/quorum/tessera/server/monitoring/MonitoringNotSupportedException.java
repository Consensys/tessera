package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;

public class MonitoringNotSupportedException extends RuntimeException {

  public MonitoringNotSupportedException(final AppType appType) {
    super(appType + " app does not support monitoring Jersey metrics");
  }
}
