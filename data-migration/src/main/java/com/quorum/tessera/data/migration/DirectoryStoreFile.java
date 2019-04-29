package com.quorum.tessera.data.migration;

import com.quorum.tessera.io.FilesDelegate;
import org.apache.commons.codec.binary.Base32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class DirectoryStoreFile implements StoreLoader {

    private final FilesDelegate fileDelegate = FilesDelegate.create();

    private Iterator<Path> fileListIterator;

    @Override
    public void load(final Path directory) throws IOException {

        //this method covers both non-directories and non-existent files
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(directory.toString() + " doesn't exist or is not a directory");
        }

        this.fileListIterator = Files.list(directory).iterator();
    }

    @Override
    public DataEntry nextEntry() {
        if (!fileListIterator.hasNext()) {
            return null;
        }

        final Path nextPath = fileListIterator.next();

        return new DataEntry(
            new Base32().decode(nextPath.toFile().getName()),
            fileDelegate.newInputStream(nextPath)
        );

    }
}
