package com.quorum.tessera.io;

import com.quorum.tessera.reflect.ReflectCallback;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UriCallback<T> {

  Logger LOGGER = LoggerFactory.getLogger(ReflectCallback.class);

  T doExecute() throws URISyntaxException;

  static <T> T execute(UriCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (URISyntaxException ex) {
      LOGGER.error(null, ex);
      throw new UncheckedIOException(new IOException(ex));
    }
  }
}
