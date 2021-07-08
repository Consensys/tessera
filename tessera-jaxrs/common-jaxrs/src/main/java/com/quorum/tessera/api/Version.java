package com.quorum.tessera.api;

public interface Version {
  default String version() {
    return getClass().getModule().getDescriptor().version().map(v -> v.toString()).get();
  }
}
