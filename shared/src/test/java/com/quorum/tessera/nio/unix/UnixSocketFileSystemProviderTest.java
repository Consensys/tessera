package com.quorum.tessera.nio.unix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnixSocketFileSystemProviderTest {

    private UnixSocketFileSystemProvider provider;

    private FileSystemProvider delegate;

    @Before
    public void onSetup() {
        this.delegate = mock(FileSystemProvider.class);

        this.provider = new UnixSocketFileSystemProvider(delegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void getScheme() {
        assertThat(provider.getScheme()).isEqualTo("unix");
    }

    @Test
    public void loadInstance() {
        URI unixUri = URI.create("unix:/somepath.socket");

        assertThat("file").isEqualTo(Paths.get(unixUri).toUri().getScheme());
    }

    @Test
    public void newFileSystem() throws IOException {

        URI uri = URI.create("unix:/bogus.ipc");
        Map<String, ?> env = new HashMap<>();
        provider.newFileSystem(uri, env);

        URI u = URI.create("file:/bogus.ipc");
        verify(delegate).newFileSystem(u, env);
    }

    @Test
    public void getFileSystem() {
        URI uri = URI.create("unix:/bogus.ipc");
        provider.getFileSystem(uri);
        URI u = URI.create("file:/bogus.ipc");
        verify(delegate).getFileSystem(u);
    }

    @Test
    public void getPath() {
        URI uri = URI.create("unix:/bogus.ipc");
        provider.getPath(uri);
        URI u = URI.create("file:/bogus.ipc");
        verify(delegate).getPath(u);
    }
}
