package com.quorum.tessera.ssl.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SSLKeyStoreLoaderTest {

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  private Path key;

  private Path cert;

  private Path invalidPemFile;

  @Before
  public void setUp() throws URISyntaxException, IOException {
    key = Paths.get(getClass().getResource("/key.pem").toURI());
    cert = Paths.get(getClass().getResource("/cert.pem").toURI());
    invalidPemFile = Paths.get(tmpDir.getRoot().getPath(), "invalid-pem-file");
    Files.createFile(invalidPemFile);
  }

  @Test
  public void testNoKeyInFile() {
    try {
      SSLKeyStoreLoader.fromPemKeyFile(invalidPemFile, cert);
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(KeyStoreException.class)
          .hasMessageContaining("NO PRIVATE KEY FOUND IN FILE");
    }
  }

  @Test
  public void testNoCertificateInFile() {
    try {
      SSLKeyStoreLoader.fromPemKeyFile(key, invalidPemFile);
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(CertificateException.class)
          .hasMessageContaining("NO CERTIFICATE FOUND IN FILE");
    }
  }
}
