package com.quorum.tessera.nio.unix;

import com.quorum.tessera.io.UriCallback;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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

  private static URI handleRelative(URI uri) {
    boolean isAbsolute = Pattern.matches("^unix:/.+$", Objects.toString(uri));
    if (isAbsolute) {
      return uri;
    }

    final String value = Objects.toString(uri);
    if (value.contains("..")) {
      // Treat double dots as invalid
      return uri;
    }
    final String cwd = Paths.get("").toAbsolutePath().toString();
    final String adjustedValue;
    if (Pattern.matches("^unix:\\..+", value)) {
      adjustedValue = value.replaceFirst("\\.", cwd);
    } else {
      adjustedValue = value.replaceFirst("unix:", "unix:" + cwd + "/");
    }
    return URI.create(adjustedValue);
  }

  private static URI convert(final URI uri) {

    final URI adjustedUri = handleRelative(uri);

    return UriCallback.execute(
        () ->
            new URI(
                "file",
                adjustedUri.getUserInfo(),
                adjustedUri.getHost(),
                adjustedUri.getPort(),
                adjustedUri.getPath(),
                adjustedUri.getQuery(),
                adjustedUri.getFragment()));
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
