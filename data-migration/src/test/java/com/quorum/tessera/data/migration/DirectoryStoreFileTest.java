package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.Ignore;

public class DirectoryStoreFileTest {

    @Test
    public void load() throws Exception {

        Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();

        Map<byte[], InputStream> results = directoryStoreFile.load(directory);

        assertThat(results).hasSize(22);

    }

    @Ignore
    @Test
    public void loadLarge() throws Exception {

        Path baseDir = Paths.get(getClass().getResource("/").toURI());

        Path directory = baseDir.resolve(UUID.randomUUID().toString());

        Files.createDirectories(directory);

        Path largeFile = Paths.get(directory.toAbsolutePath().toString(), "loadLarge");

        try (java.io.BufferedWriter writer = Files.newBufferedWriter(largeFile)) {
            IntStream.range(1, 50000000)
                .mapToObj(i -> UUID.randomUUID())
                .map(UUID::toString)
                .map(String::getBytes)
                .map(Base64.getEncoder()::encodeToString)
                .forEach(s -> {
                    try {
                        writer.write(s);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
        }

        DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();

        Map results = directoryStoreFile.load(directory);

        assertThat(results).hasSize(1);
    }

}
