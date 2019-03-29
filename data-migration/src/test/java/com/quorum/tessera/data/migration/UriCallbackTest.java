package com.quorum.tessera.data.migration;

import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class UriCallbackTest {

    @Test(expected = UncheckedIOException.class)
    public void executeIOException() throws IOException {

        UriCallback callback = () -> {
            throw new IOException();
        };

        UriCallback.execute(callback);

    }

    @Test(expected = UncheckedIOException.class)
    public void executeURISyntaxException() throws IOException {

        UriCallback callback = () -> {
            throw new URISyntaxException("","");
        };

        UriCallback.execute(callback);

    }

    @Test
    public void execute() {

        UriCallback<String> callback = () -> "RESULT";

       String result = UriCallback.execute(callback);

       assertThat(result).isEqualTo("RESULT");

    }
}
