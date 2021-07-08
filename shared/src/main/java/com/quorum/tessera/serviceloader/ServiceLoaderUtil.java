package com.quorum.tessera.serviceloader;

import java.util.ServiceLoader;

public interface ServiceLoaderUtil {

  static <T> T loadSingle(ServiceLoader<T> serviceLoader) {
    return serviceLoader.stream()
        .reduce(
            (l, r) -> {
              throw new IllegalStateException(
                  String.format(
                      "Ambiguous ServiceLoader lookup found multiple instances %s and %s.",
                      l.type().getName(), r.type().getName()));
            })
        .map(ServiceLoader.Provider::get)
        .get();
  }
}
