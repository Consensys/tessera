package com.quorum.tessera.nio.unix;

import com.quorum.tessera.io.UriCallback;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;

/** Implementation of FileSystemProvider that handles URIs with unix scheme */
public class UnixSocketFileSystemProvider extends DelegatingFileSystemProvider {

    public UnixSocketFileSystemProvider() {
        this(FileSystems.getDefault().provider());
    }

    public UnixSocketFileSystemProvider(final FileSystemProvider delegate) {
        super(delegate);
    }

    @Override
    public String getScheme() {
        return "unix";
    }

    private static URI convert(final URI uri) {

        return UriCallback.execute(
                () ->
                        new URI(
                                "file",
                                uri.getUserInfo(),
                                uri.getHost(),
                                uri.getPort(),
                                uri.getPath(),
                                uri.getQuery(),
                                uri.getFragment()));
    }

    @Override
    public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) throws IOException {
        return super.newFileSystem(convert(uri), env);
    }

    @Override
    public FileSystem getFileSystem(final URI uri) {
        return super.getFileSystem(convert(uri));
    }

    @Override
    public Path getPath(final URI uri) {
        return super.getPath(convert(uri));
    }
}
