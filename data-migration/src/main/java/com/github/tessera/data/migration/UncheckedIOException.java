
package com.github.tessera.data.migration;

import java.io.IOException;


public class UncheckedIOException extends RuntimeException {

    public UncheckedIOException(IOException ex) {
        super(ex);
    }
    
}
