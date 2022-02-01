package com.quorum.tessera.server.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.server.TesseraServer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class ServerURIFileWriterTest {

  private File directory;

  @Before
  public void beforeTest() throws IOException {
    directory = new File(Files.createTempDirectory("test").toUri());
  }

  @Test
  public void writeAllServersToOutputPath() throws IOException {
    final TesseraServer p2pServer = mock(TesseraServer.class);
    final TesseraServer q2tServer = mock(TesseraServer.class);
    final TesseraServer thirdPartyServer = mock(TesseraServer.class);

    final String p2pServerURI = "http://p2p";
    final String q2tServerURI = "http://q2t";
    final String thirdPartyServerURI = "http://thirdparty";

    final String fileName = "tessera.uris";

    when(p2pServer.getAppType()).thenReturn(AppType.P2P);
    when(p2pServer.getUri()).thenReturn(URI.create(p2pServerURI));

    when(q2tServer.getAppType()).thenReturn(AppType.Q2T);
    when(q2tServer.getUri()).thenReturn(URI.create(q2tServerURI));

    when(thirdPartyServer.getAppType()).thenReturn(AppType.THIRD_PARTY);
    when(thirdPartyServer.getUri()).thenReturn(URI.create(thirdPartyServerURI));

    final List<TesseraServer> serverList = List.of(p2pServer, q2tServer, thirdPartyServer);

    final Path path = directory.toPath();
    ServerURIFileWriter.writeServerURIsToFile(path, serverList);

    assertThat(directory.exists()).isTrue();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(directory.list().length).isEqualTo(1);

    assertThat(getFileNames(directory.listFiles())).contains(fileName);

    final List<String> fileLines = getFileLines(Path.of(directory.getPath() + "/" + fileName));

    assertThat(fileLines.size()).isEqualTo(4);

    assertThat(fileLines).contains(String.format("%s=%s", AppType.P2P, p2pServerURI));
    assertThat(fileLines).contains(String.format("%s=%s", AppType.Q2T, q2tServerURI));
    assertThat(fileLines)
        .contains(String.format("%s=%s", AppType.THIRD_PARTY, thirdPartyServerURI));
  }

  @Test
  public void writeOnlyExpectedServersToOutputPath() throws IOException {
    final TesseraServer p2pServer = mock(TesseraServer.class);
    final TesseraServer enclaveServer = mock(TesseraServer.class);

    final String p2pServerURI = "http://p2p";
    final String enclaveServerURI = "http://enclave";

    final String fileName = "tessera.uris";

    when(p2pServer.getAppType()).thenReturn(AppType.P2P);
    when(p2pServer.getUri()).thenReturn(URI.create(p2pServerURI));

    when(enclaveServer.getAppType()).thenReturn(AppType.ENCLAVE);
    when(enclaveServer.getUri()).thenReturn(URI.create(enclaveServerURI));

    final List<TesseraServer> serverList = List.of(p2pServer);

    final Path path = directory.toPath();
    ServerURIFileWriter.writeServerURIsToFile(path, serverList);

    assertThat(directory.exists()).isTrue();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(directory.list().length).isEqualTo(1);

    assertThat(getFileNames(directory.listFiles())).contains(fileName);

    assertThat(getFileLines(Path.of(directory.getPath() + "/" + fileName)))
        .contains(String.format("%s=%s", AppType.P2P, p2pServerURI));
  }

  @Test
  public void readOnlyPathShouldNotThrowException() {
    final TesseraServer p2pServer = mock(TesseraServer.class);

    final String p2pServerURI = "http://p2p";

    when(p2pServer.getAppType()).thenReturn(AppType.P2P);
    when(p2pServer.getUri()).thenReturn(URI.create(p2pServerURI));

    final List<TesseraServer> serverList = List.of(p2pServer);

    directory.setReadOnly();

    assertThat(directory.exists()).isTrue();
    assertThat(directory.canWrite()).isFalse();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(directory.list().length).isEqualTo(0);

    final Path path = directory.toPath();

    assertThatCode(() -> ServerURIFileWriter.writeServerURIsToFile(path, serverList))
        .doesNotThrowAnyException();

    assertThat(directory.list().length).isEqualTo(0);
  }

  private List<String> getFileNames(final File[] files) {
    return Stream.of(files).filter(File::isFile).map(File::getName).collect(Collectors.toList());
  }

  private List<String> getFileLines(final Path filePath) throws IOException {
    try (Stream<String> stream = Files.lines(filePath)) {
      return stream.collect(Collectors.toList());
    }
  }
}
