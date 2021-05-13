package com.quorum.tessera.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class IOCallbackTest {

  @Test
  public void doSomeIoStuff() {
    final Path path = IOCallback.execute(() -> Files.createTempFile("HELLOW", ".txt"));

    assertThat(path).isNotNull();
    assertThat(path).exists();

    IOCallback.execute(() -> Files.deleteIfExists(path));

    assertThat(path).doesNotExist();
  }

  @Test
  public void doSomeIoStuffThatThrowsAnIoException() {
    final IOException ioException = new IOException("OUCH");

    final Path path =
        mock(
            Path.class,
            iom -> {
              throw ioException;
            });

    final Throwable throwable = catchThrowable(() -> IOCallback.execute(path::isAbsolute));

    assertThat(throwable)
        .isNotNull()
        .isInstanceOf(UncheckedIOException.class)
        .hasCause(ioException);
  }
}
