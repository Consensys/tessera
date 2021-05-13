package com.quorum.tessera.ssl.trust;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CompositeTrustManagerTest {

  @Mock X509TrustManager x509TrustManager;

  @Mock TrustOnFirstUseManager trustOnFirstUseManager;

  @Mock X509Certificate certificate;

  private CompositeTrustManager trustManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    trustManager =
        new CompositeTrustManager(new TrustManager[] {x509TrustManager, trustOnFirstUseManager});
  }

  @Test
  public void testCheckServerTrustedByCA() throws CertificateException {
    doNothing().when(x509TrustManager).checkServerTrusted(any(), any());
    doThrow(new CertificateException("TOFU FAILED"))
        .when(trustOnFirstUseManager)
        .checkServerTrusted(any(), any());
    trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "s");
  }

  @Test
  public void testCheckServerTrustedByTOFU() throws CertificateException {
    doNothing().when(trustOnFirstUseManager).checkServerTrusted(any(), any());
    doThrow(new CertificateException("CA FAILED"))
        .when(x509TrustManager)
        .checkServerTrusted(any(), any());
    trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "s");
  }

  @Test
  public void testCheckServerFailedWhenBothTMFailed() throws CertificateException {
    doThrow(new CertificateException("TOFU FAILED"))
        .when(trustOnFirstUseManager)
        .checkServerTrusted(any(), any());
    doThrow(new CertificateException("CA FAILED"))
        .when(x509TrustManager)
        .checkServerTrusted(any(), any());
    try {
      trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "s");
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(CertificateException.class);
    }
  }

  @Test
  public void testCheckClientTrustedByCA() throws CertificateException {
    doNothing().when(x509TrustManager).checkClientTrusted(any(), any());
    doThrow(new CertificateException("TOFU FAILED"))
        .when(trustOnFirstUseManager)
        .checkClientTrusted(any(), any());
    trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "s");
  }

  @Test
  public void testCheckClientTrustedByTOFU() throws CertificateException {
    doNothing().when(trustOnFirstUseManager).checkClientTrusted(any(), any());
    doThrow(new CertificateException("CA FAILED"))
        .when(x509TrustManager)
        .checkClientTrusted(any(), any());
    trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "s");
  }

  @Test
  public void testCheckClientFailedWhenBothTMFailed() throws CertificateException {
    doThrow(new CertificateException("TOFU FAILED"))
        .when(trustOnFirstUseManager)
        .checkClientTrusted(any(), any());
    doThrow(new CertificateException("CA FAILED"))
        .when(x509TrustManager)
        .checkClientTrusted(any(), any());
    try {
      trustManager.checkClientTrusted(new X509Certificate[] {certificate}, "s");
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(CertificateException.class);
    }
  }

  @Test
  public void testGetAcceptIssuers() {
    assertThat(trustManager.getAcceptedIssuers()).isEmpty();
  }
}
