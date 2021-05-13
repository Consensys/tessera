package com.quorum.tessera.ssl.trust;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * If the certificate is trusted by one of the given trust managers, the connection will be
 * accepted.
 */
public class CompositeTrustManager extends AbstractTrustManager {

  private TrustManager[] trustManagers;

  public CompositeTrustManager(TrustManager[] trustManagers) {
    this.trustManagers = trustManagers.clone();
  }

  @Override
  public void checkClientTrusted(X509Certificate[] clientCertificates, String authType)
      throws CertificateException {
    for (TrustManager trustManager : trustManagers) {
      try {
        X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
        x509TrustManager.checkClientTrusted(clientCertificates, authType);
        return;
      } catch (CertificateException ex) {
        // Ignore and move on to the next trust manager
      }
    }
    throw new CertificateException("Certificate is not trusted by any of the trust managers");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] serverCertificates, String authType)
      throws CertificateException {
    for (TrustManager trustManager : trustManagers) {
      try {
        final X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
        x509TrustManager.checkServerTrusted(serverCertificates, authType);
        return;
      } catch (CertificateException ex) {
        // Ignore and move on to the next trust manager
      }
    }
    throw new CertificateException("Certificate is not trusted by any of the trust managers");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
