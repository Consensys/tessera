package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.FilesDelegate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Base32;

public class DirectoryStoreFile implements StoreLoader {

    private final FilesDelegate fileDelegate = FilesDelegate.create();
    
    @Override
    public Map<byte[], byte[]> load(Path directory) throws IOException {

        Optional.ofNullable(directory)
                .filter(p -> p.toFile().isDirectory())
                .orElseThrow(IllegalArgumentException::new);

        try (Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toMap(
                    p -> new Base32().decode(p.toFile().getName()),
                    p -> fileDelegate.readAllBytes(p)));
        }
    }

}
