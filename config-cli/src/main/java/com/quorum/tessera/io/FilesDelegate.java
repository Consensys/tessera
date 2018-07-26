
package com.quorum.tessera.io;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Uncheck invocations to java.nio.file.Files functions
 * 
 * @see java.nio.file.Files
 */
public interface FilesDelegate {
    
    static FilesDelegate create() {
        return new FilesDelegate() {};
    }
    
    /**
     * 
     * @see java.nio.file.Files#lines
     */
    default Stream<String> lines(Path path) {
        return IOCallback.execute(() -> Files.lines(path));
    }
    
    /**
     * @see java.nio.file.Files#newInputStream
     */
    default InputStream newInputStream(Path path,OpenOption... options) {
        return IOCallback.execute(() -> Files.newInputStream(path, options));
    }
    
}
