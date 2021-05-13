package com.quorum.tessera.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ReflectCallback<T> {

  Logger LOGGER = LoggerFactory.getLogger(ReflectCallback.class);

  T doExecute() throws ReflectiveOperationException;

  static <T> T execute(ReflectCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (ReflectiveOperationException ex) {
      LOGGER.error(null, ex);
      throw new ReflectException(ex);
    }
  }
}
