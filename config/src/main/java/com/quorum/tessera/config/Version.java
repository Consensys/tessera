package com.quorum.tessera.config;

public class Version {

  public static String getVersion() {
    return Version.class.getModule().getDescriptor().version().map(v -> v.toString()).get();
  }
}
