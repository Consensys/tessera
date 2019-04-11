package com.quorum.tessera.nio.unix;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class UnixSocketFileSystemProviderTest {

    private UnixSocketFileSystemProvider provider;

    private FileSystemProvider delegate;

    @Before
    public void onSetup() {
        delegate = mock(FileSystemProvider.class);
        this.provider = new UnixSocketFileSystemProvider(delegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void loadInstance() throws Exception {

        URI unixUri = URI.create("unix:/somepath.socket");

        assertThat("file").isEqualTo(Paths.get(unixUri).toUri().getScheme());
    }

    public static List<FileSystemProvider> installedProviders() {
        return FileSystemProvider.installedProviders();
    }

    @Test
    public void getScheme() {
        assertThat(provider.getScheme()).isEqualTo("unix");
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

    @Test
    public void newFileSystemPath() throws IOException {
        Path path = mock(Path.class);
        Map env = mock(Map.class);
        provider.newFileSystem(path, env);
        verify(delegate).newFileSystem(path, env);
    }

    @Test
    public void newInputStream() throws IOException {
        Path path = mock(Path.class);
        provider.newInputStream(path);
        verify(delegate).newInputStream(path);
    }

    @Test
    public void newOutputStream() throws IOException {
        Path path = mock(Path.class);
        provider.newOutputStream(path);
        verify(delegate).newOutputStream(path);
    }

    @Test
    public void newFileChannel() throws IOException {
        Path path = mock(Path.class);
        Set options = mock(Set.class);

        provider.newFileChannel(path, options);

        verify(delegate).newFileChannel(path, options);
    }

    @Test
    public void newAsynchronousFileChannel() throws IOException {
        Path path = mock(Path.class);
        Set options = mock(Set.class);
        ExecutorService executorService = mock(ExecutorService.class);

        provider.newAsynchronousFileChannel(path, options, executorService);
        verify(delegate).newAsynchronousFileChannel(path, options, executorService);
    }

    @Test
    public void newByteChannel() throws IOException {
        Path path = mock(Path.class);
        Set options = mock(Set.class);

        provider.newByteChannel(path, options);
        verify(delegate).newByteChannel(path, options);
    }

    @Test
    public void newDirectoryStream() throws IOException {
        Path path = mock(Path.class);
        DirectoryStream.Filter filter = mock(DirectoryStream.Filter.class);
        provider.newDirectoryStream(path, filter);
        verify(delegate).newDirectoryStream(path, filter);
    }

    @Test
    public void createDirectory() throws IOException {
        Path dir = mock(Path.class);
        provider.createDirectory(dir);
        verify(delegate).createDirectory(dir);
    }

    @Test
    public void createSymbolicLink() throws IOException {
        Path link = mock(Path.class);
        Path target = mock(Path.class);
        provider.createSymbolicLink(link, target);
        verify(delegate).createSymbolicLink(link, target);
    }

    @Test
    public void createLink() throws IOException {
        Path link = mock(Path.class);
        Path existing = mock(Path.class);
        provider.createLink(link, existing);
        verify(delegate).createLink(link, existing);
    }

    @Test
    public void delete() throws IOException {
        Path path = mock(Path.class);
        provider.delete(path);
        verify(delegate).delete(path);
    }

    @Test
    public void deleteIfExists() throws IOException {
        Path path = mock(Path.class);
        provider.deleteIfExists(path);
        verify(delegate).deleteIfExists(path);
    }

    @Test
    public void readSymbolicLink() throws IOException {
        Path link = mock(Path.class);
        provider.readSymbolicLink(link);
        verify(delegate).readSymbolicLink(link);
    }

    @Test
    public void copy() throws IOException {
        Path source = mock(Path.class);
        Path target = mock(Path.class);
        provider.copy(source, target);
        verify(delegate).copy(source, target);
    }

    @Test
    public void move() throws IOException {
        Path source = mock(Path.class);
        Path target = mock(Path.class);
        provider.move(source, target);
        verify(delegate).move(source, target);
    }

    @Test
    public void isSameFile() throws IOException {
        Path path = mock(Path.class);
        Path path2 = mock(Path.class);
        provider.isSameFile(path, path2);
        verify(delegate).isSameFile(path, path2);
    }

    @Test
    public void isHidden() throws IOException {
        Path path = mock(Path.class);
        provider.isHidden(path);
        verify(delegate).isHidden(path);
    }

    @Test
    public void getFileStore() throws IOException {
        Path path = mock(Path.class);
        provider.getFileStore(path);
        verify(delegate).getFileStore(path);
    }

    @Test
    public void checkAccess() throws IOException {
        Path path = mock(Path.class);
        provider.checkAccess(path);
        verify(delegate).checkAccess(path);
    }

    @Test
    public void getFileAttributeView() {
        Path path = mock(Path.class);
        Class type = String.class;
        provider.getFileAttributeView(path, type);
        verify(delegate).getFileAttributeView(path, type);

    }
    
    @Test
    public void readAttributes() throws IOException {
        Path path = mock(Path.class);
        Class type = String.class;
        provider.readAttributes(path, type);
        verify(delegate).readAttributes(path, type);
    }

    @Test
    public void readAttributes2() throws IOException {
        Path path = mock(Path.class);
        String attributes = UUID.randomUUID().toString();
        provider.readAttributes(path, attributes);
        verify(delegate).readAttributes(path, attributes);
    }

    @Test
    public void setAttribute() throws IOException {
        Path path = mock(Path.class);
        String attribute = UUID.randomUUID().toString();
        Object value = mock(Object.class);
        provider.setAttribute(path, attribute,value);
        verify(delegate).setAttribute(path, attribute,value);
    }

}
