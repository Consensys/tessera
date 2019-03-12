package com.quorum.tessera.nio.unix;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;


public interface UriCallback<T> {
    
    T doExecute() throws URISyntaxException;
    
    static <T> T execute(UriCallback<T> callback) {
        try{
            return callback.doExecute();
        } catch (URISyntaxException ex) {
             throw new UncheckedIOException(new IOException(ex));
        }
    }
}
