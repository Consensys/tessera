package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;

public interface UriCallback<T> {

    T doExecute() throws IOException, URISyntaxException;

    static <T> T execute(UriCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException ex) {
            throw new UncheckedIOException(new IOException(ex.getMessage()));
        }

    }

}
