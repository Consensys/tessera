package com.quorum.tessera.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class IOCallbackTest {

    @Test
    public void executeHappyCase() {
        IOCallback<Boolean> callback = () -> true;

        boolean result = IOCallback.execute(callback);
        assertThat(result).isTrue();

    }

    @Test(expected = UncheckedIOException.class)
    public void executeThrowsIOException() throws Exception {
        IOCallback callback = mock(IOCallback.class);
        doThrow(IOException.class).when(callback).doExecute();
        IOCallback.execute(callback);

    }

}
