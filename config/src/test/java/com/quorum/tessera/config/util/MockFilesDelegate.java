package com.quorum.tessera.config.util;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import static org.mockito.Mockito.mock;

public class MockFilesDelegate implements FilesDelegate {

    private static FilesDelegate delegate;

    public MockFilesDelegate() {
        //Default instance
        if (delegate == null) {
            delegate = new FilesDelegate() {
            };
        }
    }

    @Override
    public boolean notExists(Path path, LinkOption... options) {
        return delegate.notExists(path, options);
    }

    @Override
    public boolean deleteIfExists(Path path) {
        return delegate.deleteIfExists(path);
    }

    @Override
    public Path createFile(Path path, FileAttribute... attributes) {
        return delegate.createFile(path, attributes);
    }

    public static FilesDelegate setUpMock() {

        delegate = mock(FilesDelegate.class);

        return delegate;
    }

    public static void tearDownMock() {
        delegate = null;
    }

}
