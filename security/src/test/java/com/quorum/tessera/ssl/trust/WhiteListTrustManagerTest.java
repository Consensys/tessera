package com.quorum.tessera.ssl.trust;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.ssl.util.CertificateUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WhiteListTrustManagerTest {

  private WhiteListTrustManager trustManager;

  private Path knownHosts;

  @Mock private X509Certificate certificate;

  @Before
  public void setUp() throws IOException, CertificateException {
    MockitoAnnotations.openMocks(this);
    when(certificate.getEncoded()).thenReturn("thumbprint".getBytes(UTF_8));
    X500Principal cn = new X500Principal("CN=localhost");
    when(certificate.getSubjectX500Principal()).thenReturn(cn);
    knownHosts = Files.createTempFile("test", "knownHosts");

    try (BufferedWriter writer = Files.newBufferedWriter(knownHosts, StandardOpenOption.APPEND)) {
      writer.write("someaddress somethumbprint");
      writer.newLine();
      writer.write("localhost" + " " + CertificateUtil.create().thumbPrint(certificate));
      writer.newLine();
    }

    trustManager = new WhiteListTrustManager(knownHosts);
  }

  @After
  public void after() {
    verifyNoMoreInteractions(certificate);
  }

  @Test
  public void testLoadCertificatesFromWhiteListFile() throws CertificateException {

    trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "str");
    trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "str");

    verify(certificate, times(3)).getEncoded();
    verify(certificate, times(2)).getSubjectX500Principal();
  }

  @Test
  public void testCertificatesNotInWhiteList() throws CertificateException {
    when(certificate.getEncoded()).thenReturn("some-other-thumbprint".getBytes(UTF_8));
    try {
      trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "str");
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(CertificateException.class).hasMessage("Connections not allowed");
    }
    verify(certificate, times(2)).getEncoded();
    verify(certificate).getSubjectX500Principal();
  }

  @Test
  public void testGetAcceptIssuers() throws CertificateEncodingException {
    assertThat(trustManager.getAcceptedIssuers()).isEmpty();
    verify(certificate).getEncoded();
  }
}
