package com.quorum.tessera.ssl.trust;

import com.quorum.tessera.ssl.util.CertificateUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.cryptacular.util.CertUtil;

public class TrustOnFirstUseManager extends AbstractTrustManager {

  public TrustOnFirstUseManager(Path knownHosts) throws IOException {
    super(knownHosts);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] clientCertificates, String authType)
      throws CertificateException {
    checkTrusted(clientCertificates);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] serverCertificates, String authType)
      throws CertificateException {
    checkTrusted(serverCertificates);
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }

  private void checkTrusted(X509Certificate[] x509Certificates) throws CertificateException {
    final X509Certificate certificate = x509Certificates[0];
    final String thumbPrint = CertificateUtil.create().thumbPrint(certificate);
    final String address = CertUtil.subjectCN(certificate);

    if (!certificateExistsInKnownHosts(address)) {
      try {
        addServerToKnownHostsList(address, thumbPrint);
      } catch (IOException ex) {
        throw new CertificateException(
            "Failed to save address and certificate fingerprint to whitelist. Cause by ", ex);
      }
    } else {
      if (!certificateValidForKnownHost(address, thumbPrint)) {
        throw new CertificateException(
            "This address has been associated with a different certificate");
      }
    }
  }
}
