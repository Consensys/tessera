package com.quorum.tessera.data.migration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.bouncycastle.util.encoders.Hex;

/**
 * Assumes that user has exported data from bdb using db_dump
 * <pre>
 *  db_dump -f exported.txt c1/cn§.db/payload.db
 * </pre>
 *
 */
public class BdbDumpFile implements StoreLoader {

    @Override
    public Map<byte[], InputStream> load(Path inputFile) throws IOException {

        Map<byte[], InputStream> results = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {

            while (true) {
                String line = reader.readLine();
                if (Objects.isNull(line)) {
                    break;
                }

                if (!line.startsWith(" ")) {
                    continue;
                }

                final String key = line.trim();

                final String value = reader.readLine();
                
                InputStream inputStream = Optional.of(value)
                    .map(Hex::decode)
                    .map(ByteArrayInputStream::new)
                    .get();
                results.put(Base64.getDecoder().decode(Hex.decode(key)), inputStream);
            }
            return Collections.unmodifiableMap(results);

        }
    }

}
