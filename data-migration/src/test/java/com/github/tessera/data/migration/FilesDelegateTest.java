package com.github.tessera.data.migration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class FilesDelegateTest {
    
    private final FilesDelegate filesDelegate = FilesDelegate.create();
    
    @Test
    public void readAllBytes() {
        
        final IOException ioException = new IOException("OUCH!!");
        
        Path path = mock(Path.class, invocation -> {
            throw ioException;
        });
        
        try {
            filesDelegate.readAllBytes(path);
            failBecauseExceptionWasNotThrown(UncheckedIOException.class);
        } catch (UncheckedIOException ex) {
            assertThat(ex).hasCauseInstanceOf(IOException.class);
            assertThat(ex.getCause()).isSameAs(ioException);
        }        
    }
    
}
