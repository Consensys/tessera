package com.quorum.tessera.version;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface ApiVersion {

  String getVersion();

  static List<String> versions() {
    return ServiceLoader.load(ApiVersion.class).stream()
        .map(ServiceLoader.Provider::get)
        .map(ApiVersion::getVersion)
        .filter(Objects::nonNull)
        .sorted()
        .collect(Collectors.toUnmodifiableList());
  }
}
