package com.quorum.tessera.nio.unix;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DelegatingFileSystemProviderTest {

  private DelegatingFileSystemProvider provider;

  private FileSystemProvider delegate;

  @Before
  public void onSetup() {
    this.delegate = mock(FileSystemProvider.class);

    this.provider =
        new DelegatingFileSystemProvider(delegate) {
          @Override
          public String getScheme() {
            return null;
          }
        };
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void newFileSystem() throws IOException {
    final URI uri = URI.create("unix:/bogus.ipc");
    final Map<String, ?> env = new HashMap<>();

    provider.newFileSystem(uri, env);

    verify(delegate).newFileSystem(uri, env);
  }

  @Test
  public void getFileSystem() {
    final URI uri = URI.create("unix:/bogus.ipc");

    provider.getFileSystem(uri);

    verify(delegate).getFileSystem(uri);
  }

  @Test
  public void getPath() {
    final URI uri = URI.create("unix:/bogus.ipc");

    provider.getPath(uri);

    verify(delegate).getPath(uri);
  }

  @Test
  public void newByteChannel() throws IOException {
    final Path path = mock(Path.class);
    final Set<OpenOption> options = new HashSet<>();

    provider.newByteChannel(path, options);

    verify(delegate).newByteChannel(path, options);
  }

  @Test
  public void newDirectoryStream() throws IOException {
    final Path path = mock(Path.class);
    final DirectoryStream.Filter<Path> filter = mock(DirectoryStream.Filter.class);

    provider.newDirectoryStream(path, filter);

    verify(delegate).newDirectoryStream(path, filter);
  }

  @Test
  public void createDirectory() throws IOException {
    final Path dir = mock(Path.class);

    provider.createDirectory(dir);

    verify(delegate).createDirectory(dir);
  }

  @Test
  public void delete() throws IOException {
    final Path path = mock(Path.class);

    provider.delete(path);

    verify(delegate).delete(path);
  }

  @Test
  public void newFileSystemWithPath() throws IOException {
    final Path path = mock(Path.class);
    final Map<String, ?> env = new HashMap<>();

    provider.newFileSystem(path, env);

    verify(delegate).newFileSystem(path, env);
  }

  @Test
  public void newInputStream() throws IOException {
    final Path path = mock(Path.class);

    provider.newInputStream(path);

    verify(delegate).newInputStream(path);
  }

  @Test
  public void newOutputStream() throws IOException {
    final Path path = mock(Path.class);

    provider.newOutputStream(path);

    verify(delegate).newOutputStream(path);
  }

  @Test
  public void newFileChannel() throws IOException {
    final Path path = mock(Path.class);
    final Set<OpenOption> options = new HashSet<>();

    provider.newFileChannel(path, options);

    verify(delegate).newFileChannel(path, options);
  }

  @Test
  public void newAsynchronousFileChannel() throws IOException {
    final Path path = mock(Path.class);
    final Set<OpenOption> options = new HashSet<>();
    final ExecutorService executorService = mock(ExecutorService.class);

    provider.newAsynchronousFileChannel(path, options, executorService);

    verify(delegate).newAsynchronousFileChannel(path, options, executorService);
  }

  @Test
  public void createSymbolicLink() throws IOException {
    final Path link = mock(Path.class);
    final Path target = mock(Path.class);

    provider.createSymbolicLink(link, target);

    verify(delegate).createSymbolicLink(link, target);
  }

  @Test
  public void createLink() throws IOException {
    final Path link = mock(Path.class);
    final Path existing = mock(Path.class);

    provider.createLink(link, existing);

    verify(delegate).createLink(link, existing);
  }

  @Test
  public void deleteIfExists() throws IOException {
    final Path path = mock(Path.class);

    provider.deleteIfExists(path);

    verify(delegate).deleteIfExists(path);
  }

  @Test
  public void readSymbolicLink() throws IOException {
    final Path link = mock(Path.class);

    provider.readSymbolicLink(link);

    verify(delegate).readSymbolicLink(link);
  }

  @Test
  public void copy() throws IOException {
    final Path source = mock(Path.class);
    final Path target = mock(Path.class);

    provider.copy(source, target);

    verify(delegate).copy(source, target);
  }

  @Test
  public void move() throws IOException {
    final Path source = mock(Path.class);
    final Path target = mock(Path.class);

    provider.move(source, target);

    verify(delegate).move(source, target);
  }

  @Test
  public void isSameFile() throws IOException {
    final Path path = mock(Path.class);
    final Path path2 = mock(Path.class);

    provider.isSameFile(path, path2);

    verify(delegate).isSameFile(path, path2);
  }

  @Test
  public void isHidden() throws IOException {
    final Path path = mock(Path.class);

    provider.isHidden(path);

    verify(delegate).isHidden(path);
  }

  @Test
  public void getFileStore() throws IOException {
    final Path path = mock(Path.class);

    provider.getFileStore(path);

    verify(delegate).getFileStore(path);
  }

  @Test
  public void checkAccess() throws IOException {
    final Path path = mock(Path.class);

    provider.checkAccess(path);

    verify(delegate).checkAccess(path);
  }

  @Test
  public void getFileAttributeView() {
    final Path path = mock(Path.class);
    final Class<FileAttributeView> type = FileAttributeView.class;

    provider.getFileAttributeView(path, type);

    verify(delegate).getFileAttributeView(path, type);
  }

  @Test
  public void readAttributesUsingClass() throws IOException {
    final Path path = mock(Path.class);
    final Class<BasicFileAttributes> type = BasicFileAttributes.class;

    provider.readAttributes(path, type);

    verify(delegate).readAttributes(path, type);
  }

  @Test
  public void readAttributesUsingStringAttributes() throws IOException {
    final Path path = mock(Path.class);
    final String attributes = UUID.randomUUID().toString();

    provider.readAttributes(path, attributes);

    verify(delegate).readAttributes(path, attributes);
  }

  @Test
  public void setAttribute() throws IOException {
    final Path path = mock(Path.class);
    final String attribute = UUID.randomUUID().toString();
    final Object value = new Object();

    provider.setAttribute(path, attribute, value);

    verify(delegate).setAttribute(path, attribute, value);
  }
}
