package com.quorum.tessera.partyinfo.node;

import java.util.Set;

public interface VersionInfo {

  Set<String> supportedApiVersions();

  static VersionInfo from(Set<String> versions) {
    return () -> versions;
  }
}
