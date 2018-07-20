package com.github.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DirectoryStoreFile implements StoreLoader {

    private final FilesDelegate fileDelegate = FilesDelegate.create();
    
    @Override
    public Map<String, byte[]> load(Path directory) throws IOException {

        Optional.ofNullable(directory)
                .filter(p -> Files.isDirectory(p))
                .orElseThrow(IllegalArgumentException::new);
        
        return Files.list(directory)
                .collect(Collectors.toMap(p -> p.toFile().getName(), p -> fileDelegate.readAllBytes(p)));

    }

}
