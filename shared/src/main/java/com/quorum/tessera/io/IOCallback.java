package com.quorum.tessera.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback and template function to uncheck IO exceptions
 *
 * @param <T> the function to execute that throws an {@link IOException}
 */
@FunctionalInterface
public interface IOCallback<T> {

  Logger LOGGER = LoggerFactory.getLogger(IOCallback.class);

  T doExecute() throws IOException;

  static <T> T execute(IOCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (final IOException ex) {
      LOGGER.debug(null, ex);
      throw new UncheckedIOException(ex);
    }
  }
}
