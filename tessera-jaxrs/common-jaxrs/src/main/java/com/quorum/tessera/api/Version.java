package com.quorum.tessera.api;

import java.util.ServiceLoader;

public interface Version {

  static String getVersion() {
    return ServiceLoader.load(Version.class).findFirst().orElse(new Version() {}).version();
  }

  default String version() {
    return Version.class.getPackage().getSpecificationVersion();
  }
}
