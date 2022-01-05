package com.quorum.tessera.cli.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
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

public class ServerURIOutputMixinTest {

  private ServerURIOutputMixin serverURIOutputMixin;
  private File directory;

  @Before
  public void beforeTest() throws IOException {
    serverURIOutputMixin = new ServerURIOutputMixin();
    directory = new File(Files.createTempDirectory("test").toUri());
  }

  @Test
  public void testConfigPathIsUpdatedWithSpecifiedPath() {
    final Path testPath = Path.of("/testPath");
    final Config config = new Config();
    assertThat(config.getOutputServerURIPath()).isNull();
    serverURIOutputMixin.updateConfig(testPath, config);
    assertThat(config.getOutputServerURIPath()).isEqualTo(testPath);
  }

  @Test
  public void testConfigPathStaysNullWithoutSpecifiedPath() {
    final Config config = new Config();
    assertThat(config.getOutputServerURIPath()).isNull();
    serverURIOutputMixin.updateConfig(null, config);
    assertThat(config.getOutputServerURIPath()).isNull();
  }

  @Test
  public void writeAllServersToOutputPath() throws IOException {
    final TesseraServer p2pServer = mock(TesseraServer.class);
    final TesseraServer q2tServer = mock(TesseraServer.class);
    final TesseraServer thirdPartyServer = mock(TesseraServer.class);

    final String p2pServerURI = "http://p2p";
    final String q2tServerURI = "http://q2t";
    final String thirdPartyServerURI = "http://thirdparty";

    final String p2pURIFilename = "p2pServer.uri";
    final String q2tURIFilename = "q2tServer.uri";
    final String thirdPartyURIFilename = "thirdPartyServer.uri";

    when(p2pServer.getAppType()).thenReturn(AppType.P2P);
    when(p2pServer.getUri()).thenReturn(URI.create(p2pServerURI));

    when(q2tServer.getAppType()).thenReturn(AppType.Q2T);
    when(q2tServer.getUri()).thenReturn(URI.create(q2tServerURI));

    when(thirdPartyServer.getAppType()).thenReturn(AppType.THIRD_PARTY);
    when(thirdPartyServer.getUri()).thenReturn(URI.create(thirdPartyServerURI));

    final List<TesseraServer> serverList = List.of(p2pServer, q2tServer, thirdPartyServer);

    final Path path = directory.toPath();
    ServerURIOutputMixin.writeServerURIsToFile(path, serverList);

    assertThat(directory.exists()).isTrue();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(directory.list().length).isEqualTo(3);

    assertThat(getFileNames(directory.listFiles()))
        .contains(p2pURIFilename, q2tURIFilename, thirdPartyURIFilename);

    assertThat(getFileLine(Path.of(directory.getPath() + "/" + p2pURIFilename)))
        .contains(p2pServerURI);
    assertThat(getFileLine(Path.of(directory.getPath() + "/" + q2tURIFilename)))
        .contains(q2tServerURI);
    assertThat(getFileLine(Path.of(directory.getPath() + "/" + thirdPartyURIFilename)))
        .contains(thirdPartyServerURI);
  }

  @Test
  public void writeOnlyExpectedServersToOutputPath() throws IOException {
    final TesseraServer p2pServer = mock(TesseraServer.class);
    final TesseraServer enclaveServer = mock(TesseraServer.class);

    final String p2pServerURI = "http://p2p";
    final String enclaveServerURI = "http://enclave";

    final String p2pURIFilename = "p2pServer.uri";

    when(p2pServer.getAppType()).thenReturn(AppType.P2P);
    when(p2pServer.getUri()).thenReturn(URI.create(p2pServerURI));

    when(enclaveServer.getAppType()).thenReturn(AppType.ENCLAVE);
    when(enclaveServer.getUri()).thenReturn(URI.create(enclaveServerURI));

    final List<TesseraServer> serverList = List.of(p2pServer);

    final Path path = directory.toPath();
    ServerURIOutputMixin.writeServerURIsToFile(path, serverList);

    assertThat(directory.exists()).isTrue();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(directory.list().length).isEqualTo(1);

    assertThat(getFileNames(directory.listFiles())).contains(p2pURIFilename);

    assertThat(getFileLine(Path.of(directory.getPath() + "/" + p2pURIFilename)))
        .contains(p2pServerURI);
  }

  private List<String> getFileNames(final File[] files) {
    return Stream.of(files).filter(File::isFile).map(File::getName).collect(Collectors.toList());
  }

  private String getFileLine(final Path filePath) throws IOException {
    try (Stream<String> stream = Files.lines(filePath)) {
      return stream.findFirst().get();
    }
  }
}
