package com.github.tessera.ssl.trust;

import com.github.tessera.ssl.util.CertificateUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustOnFirstUseManager extends AbstractTrustManager {

    public TrustOnFirstUseManager(Path knownHosts) throws IOException {
        super(knownHosts);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        checkTrusted(x509Certificates);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        checkTrusted(x509Certificates);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    private void checkTrusted(X509Certificate[] x509Certificates) throws CertificateException {
        final X509Certificate certificate = x509Certificates[0];
        final String thumbPrint = CertificateUtil.create().thumbPrint(certificate);

        if (!certificateExistsInKnownHosts(thumbPrint)) {
            try {
                addServerToKnownHostsList(thumbPrint);
            } catch (IOException ex) {
                throw new CertificateException("Failed to save address and certificate fingerprint to whitelist. Cause by ", ex);
            }
        }
    }
}
