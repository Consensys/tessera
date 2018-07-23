
package com.github.tessera.io;

import java.nio.file.Files;
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
    
    default Stream<String> lines(Path path) {
        return IOCallback.execute(() -> Files.lines(path));
    }
    
}
