package com.quorum.tessera.data.migration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 * Assumes that user has exported data from bdb using db_dump
 *
 * <pre>
 *  db_dump -f exported.txt c1/cn§.db/payload.db
 * </pre>
 */
public class BdbDumpFile implements StoreLoader {

  private BufferedReader reader;

  private DataEntry nextEntry;

  @Override
  public void load(final Path inputFile) throws IOException {

    // Handles both non-regular files and non-existent files
    if (!Files.isRegularFile(inputFile)) {
      throw new IllegalArgumentException(inputFile.toString() + " doesn't exist or is not a file");
    }

    this.reader = Files.newBufferedReader(inputFile);

    final String firstKey;
    while (true) {
      final String line = reader.readLine();

      if (line == null) {
        // apparently there was nothing in this file, close and return
        this.nextEntry = null;
        this.reader.close();
        return;
      }

      if (line.startsWith(" ")) {
        // found the first data entry, stop looping over the headers
        firstKey = line;
        break;
      }
    }

    final String firstValue = reader.readLine();

    this.nextEntry =
        new DataEntry(
            Base64.getDecoder().decode(Hex.decode(firstKey.trim())),
            new ByteArrayInputStream(Hex.decode(firstValue)));
  }

  @Override
  public DataEntry nextEntry() throws IOException {
    if (this.nextEntry == null) {
      return null;
    }

    final DataEntry oldEntry = this.nextEntry;

    final String nextKey = reader.readLine();

    if (nextKey == null || !nextKey.startsWith(" ")) {
      this.nextEntry = null;
      this.reader.close();
    } else {
      final String nextValue = reader.readLine();

      this.nextEntry =
          new DataEntry(
              Base64.getDecoder().decode(Hex.decode(nextKey.trim())),
              new ByteArrayInputStream(Hex.decode(nextValue)));
    }

    return oldEntry;
  }
}
