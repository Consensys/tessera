package com.quorum.tessera;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ServiceLoaderUtil {

  static <T> Optional<T> load(Class<T> type) {
    return ServiceLoaderUtil.loadAll(type).findFirst();
  }

  static <T> Stream<T> loadAll(Class<T> type) {
    // TODO: Java 9 defines a native stream method for the service loader, use that instead
    return StreamSupport.stream(ServiceLoader.load(type).spliterator(), false);
  }
}
