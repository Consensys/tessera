package com.quorum.tessera.io;

import org.junit.Test;

import java.io.UncheckedIOException;
import java.net.URISyntaxException;

public class UriCallbackTest {

    @Test(expected = UncheckedIOException.class)
    public void executeThrowsURISyntaxException() {

        UriCallback.execute(() -> {
            throw new URISyntaxException("", "");
        });

    }

}
