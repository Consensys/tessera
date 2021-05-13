package com.quorum.tessera.ssl.trust;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TrustOnFirstUseManagerTest {

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  private TrustOnFirstUseManager trustManager;

  private Path knownHosts;

  private X509Certificate certificate;

  @Before
  public void setUp() {
    this.certificate = mock(X509Certificate.class);

    this.knownHosts = Paths.get(tmpDir.getRoot().getPath(), "parent", "knownHosts");
  }

  @After
  public void after() {
    verifyNoMoreInteractions(certificate);
  }

  @Test
  public void testAddThumbPrintToKnownHostsList() throws CertificateException, IOException {
    trustManager = new TrustOnFirstUseManager(knownHosts);

    when(certificate.getEncoded()).thenReturn("certificate".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);

    assertThat(Files.exists(knownHosts)).isFalse();

    trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "s");

    assertThat(Files.exists(knownHosts)).isTrue();

    trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "s");
    verify(certificate, times(2)).getEncoded();
    verify(certificate, times(2)).getSubjectX500Principal();
  }

  @Test
  public void testFailedToGenerateWhiteListFile() throws IOException, CertificateEncodingException {
    Path anotherFile = Paths.get(tmpDir.getRoot().getPath(), "parent", "anotherFile");
    tmpDir.getRoot().setWritable(false);

    trustManager = new TrustOnFirstUseManager(anotherFile);

    when(certificate.getEncoded()).thenReturn("certificate".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);

    try {
      trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "str");
      failBecauseExceptionWasNotThrown(IOException.class);
    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(CertificateException.class)
          .hasMessageContaining("Failed to save address and certificate fingerprint to whitelist");
    }

    verify(certificate).getEncoded();
    verify(certificate).getSubjectX500Principal();
    tmpDir.getRoot().setWritable(true);
  }

  @Test
  public void testGenerateWhiteListFileNoParentDirectory()
      throws IOException, CertificateException {
    Path anotherFile = Paths.get("anotherFileInRootDir");
    trustManager = new TrustOnFirstUseManager(anotherFile);

    when(certificate.getEncoded()).thenReturn("certificate".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);

    assertThat(Files.exists(anotherFile)).isFalse();

    trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "str");

    assertThat(Files.exists(anotherFile)).isTrue();

    verify(certificate).getEncoded();
    verify(certificate).getSubjectX500Principal();

    Files.deleteIfExists(anotherFile);

    assertThat(Files.exists(anotherFile)).isFalse();
  }

  @Test
  public void testAddFingerPrintFailedToWrite() throws CertificateException, IOException {

    Path notWritable = Paths.get(tmpDir.getRoot().getPath(), "notWritable");

    Files.createFile(notWritable);
    Files.setPosixFilePermissions(notWritable, PosixFilePermissions.fromString("r--------"));

    trustManager = new TrustOnFirstUseManager(notWritable);

    X509Certificate certificate = mock(X509Certificate.class);
    when(certificate.getEncoded()).thenReturn("certificate".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);

    try {
      trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "s");
      trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "s");

      failBecauseExceptionWasNotThrown(CertificateException.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(CertificateException.class);
    }
  }

  @Test
  public void testCertificateNotValidForRecognisedAddress()
      throws CertificateException, IOException {
    testAddThumbPrintToKnownHostsList();

    when(certificate.getEncoded()).thenReturn("ADifferentCertificate".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);

    try {
      trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "str");
      failBecauseExceptionWasNotThrown(IOException.class);
    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(CertificateException.class)
          .hasMessageContaining("This address has been associated with a different certificate");
    }

    verify(certificate, times(3)).getEncoded();
    verify(certificate, times(3)).getSubjectX500Principal();
  }

  @Test
  public void testGetAcceptIssuers() throws IOException {
    trustManager = new TrustOnFirstUseManager(knownHosts);
    assertThat(trustManager.getAcceptedIssuers()).isEmpty();
  }
}
