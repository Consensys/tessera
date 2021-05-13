package com.quorum.tessera.io;

import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class UriCallbackTest {

  @Test(expected = UncheckedIOException.class)
  public void executeThrowsURISyntaxException() {

    UriCallback.execute(
        () -> {
          throw new URISyntaxException("", "");
        });
  }
}
