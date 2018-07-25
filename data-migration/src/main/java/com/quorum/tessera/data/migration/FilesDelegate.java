
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public interface FilesDelegate {
    
    static FilesDelegate create() {
        return new FilesDelegate() {};
    }
    
    default byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
}
