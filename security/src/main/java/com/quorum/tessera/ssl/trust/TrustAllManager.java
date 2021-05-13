package com.quorum.tessera.ssl.trust;

import java.security.cert.X509Certificate;

public class TrustAllManager extends AbstractTrustManager {

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
    // Do nothing, let all pass
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
    // Do nothing, let all pass
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
