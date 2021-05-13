package com.quorum.tessera.ssl.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.junit.Test;

public class CertificateUtilTest {

  @Test
  public void testGenerateThumbPrint() throws CertificateException {

    X509Certificate certificate = mock(X509Certificate.class);
    when(certificate.getEncoded()).thenReturn("some".getBytes(UTF_8));

    assertThat(CertificateUtil.create().thumbPrint(certificate))
        .isEqualTo("eb875812858d27b22cb2b75f992dffadc1b05c66");
  }

  @Test
  public void testGenerateThrowException() {
    X509Certificate certificate = mock(X509Certificate.class);
    try {
      CertificateUtil.create().thumbPrint(certificate);
      failBecauseExceptionWasNotThrown(Exception.class);

    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(CertificateException.class)
          .hasMessageContaining("Cannot generate thumbprint for this certificate");
    }
  }
}
