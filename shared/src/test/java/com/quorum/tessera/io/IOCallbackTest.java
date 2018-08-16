package com.quorum.tessera.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

public class IOCallbackTest {

    @Test
    public void doSomeIoStuff() {
        Path path = IOCallback.execute(() -> Files.createTempFile("HELLOW", ".txt"));

        assertThat(path).isNotNull();
        assertThat(path).exists();

        IOCallback.execute(() -> Files.deleteIfExists(path));

        assertThat(path).doesNotExist();

    }

    @Test
    public void doSomeIoStuffThatThrowsAnIoException() {

        final IOException ioException = new IOException("OUCH");

        final Path path = mock(Path.class, (Answer<Object>) (iom) -> {
            throw ioException;
        });

        try {
            IOCallback.execute(() -> path.isAbsolute());
            failBecauseExceptionWasNotThrown(UncheckedIOException.class);
        } catch (UncheckedIOException ex) {
            assertThat(ex).hasCause(ioException);
        }
    }

}
