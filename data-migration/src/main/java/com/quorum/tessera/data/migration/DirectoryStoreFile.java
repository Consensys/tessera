package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryStoreFile implements StoreLoader {

    private final FilesDelegate fileDelegate = FilesDelegate.create();
    
    @Override
    public Map<byte[], byte[]> load(Path directory) throws IOException {

        Optional.ofNullable(directory)
                .filter(p -> p.toFile().isDirectory())
                .orElseThrow(IllegalArgumentException::new);

        try (Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toMap(
                    p -> Base64.getDecoder().decode(p.toFile().getName()),
                    p -> fileDelegate.readAllBytes(p)));
        }
    }

}
