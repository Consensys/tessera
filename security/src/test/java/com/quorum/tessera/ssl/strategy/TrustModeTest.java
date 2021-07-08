package com.quorum.tessera.ssl.strategy;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.quorum.tessera.ssl.context.model.SSLContextProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Test;

public class TrustModeTest {

  private Path tmpFile;

  private Path tmpKnownHosts;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Before
  public void setUp() throws IOException {
    final Path folder = Files.createTempDirectory("tmp");

    this.tmpFile = folder.resolve("tmpFile");
    this.tmpKnownHosts = folder.resolve("knownHosts");
  }

  @Test
  public void testNone() throws IOException, GeneralSecurityException, OperatorCreationException {
    final SSLContextProperties props =
        new SSLContextProperties(
            "http://localhost:8080",
            tmpFile,
            "quorum".toCharArray(),
            null,
            null,
            tmpFile,
            "quorum".toCharArray(),
            null,
            tmpKnownHosts);

    assertThat(TrustMode.NONE.createSSLContext(props)).isNotNull();
  }

  @Test
  public void testWhiteList()
      throws IOException, GeneralSecurityException, OperatorCreationException {
    final SSLContextProperties props =
        new SSLContextProperties(
            "http://localhost:8080",
            tmpFile,
            "quorum".toCharArray(),
            null,
            null,
            tmpFile,
            "quorum".toCharArray(),
            null,
            tmpKnownHosts);

    assertThat(TrustMode.getValueIfPresent("WHITELIST").get().createSSLContext(props)).isNotNull();
  }

  @Test
  public void testCA() throws IOException, GeneralSecurityException, OperatorCreationException {
    final SSLContextProperties props =
        new SSLContextProperties(
            "http://localhost:8080",
            tmpFile,
            "quorum".toCharArray(),
            null,
            null,
            tmpFile,
            "quorum".toCharArray(),
            null,
            tmpKnownHosts);

    assertThat(TrustMode.getValueIfPresent("CA").get().createSSLContext(props)).isNotNull();
  }

  @Test
  public void testTOFU() throws IOException, GeneralSecurityException, OperatorCreationException {
    final SSLContextProperties props =
        new SSLContextProperties(
            "http://localhost:8080",
            tmpFile,
            "quorum".toCharArray(),
            null,
            null,
            tmpFile,
            "quorum".toCharArray(),
            null,
            tmpKnownHosts);

    assertThat(TrustMode.getValueIfPresent("TOFU").get().createSSLContext(props)).isNotNull();
  }

  @Test
  public void testCAOrTOFU()
      throws IOException, GeneralSecurityException, OperatorCreationException {
    final SSLContextProperties props =
        new SSLContextProperties(
            "http://localhost:8080",
            tmpFile,
            "quorum".toCharArray(),
            null,
            null,
            tmpFile,
            "quorum".toCharArray(),
            null,
            tmpKnownHosts);

    assertThat(TrustMode.getValueIfPresent("CA_OR_TOFU").get().createSSLContext(props)).isNotNull();
  }

  @Test
  public void testInvalidMode() {
    assertThat(TrustMode.getValueIfPresent("SOMETHING").isPresent()).isFalse();
  }
}
