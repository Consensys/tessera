
package com.quorum.tessera.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Callback and template function t uncheck IO exceptions
 * 
 * @param <T> 
 */
@FunctionalInterface
public interface IOCallback<T> {
    
    Logger LOGGER = LoggerFactory.getLogger(IOCallback.class);
    
    T doExecute() throws IOException;

    static <T> T execute(IOCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (IOException ex) {
            LOGGER.debug(null, ex);
            throw new UncheckedIOException(ex);
        }
    }
        
    
}
