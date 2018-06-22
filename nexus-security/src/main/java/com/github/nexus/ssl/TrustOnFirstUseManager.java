package com.github.nexus.ssl;

import com.github.nexus.ssl.util.CertificateUtil;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustOnFirstUseManager extends ExtendedTrustManager {

    public TrustOnFirstUseManager(File knownHosts) throws IOException {
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

    private void checkTrusted(X509Certificate[] x509Certificates) throws CertificateException{
        final X509Certificate certificate = x509Certificates[0];
        final String fingerPrint = CertificateUtil.generateFingerprint(certificate);

        if (!certificateExistsInKnownHosts(fingerPrint)){
            try {
                addServerToKnownHostsList(fingerPrint);
            }
            catch (IOException ex){
                throw new CertificateException("Failed to save address and certificate fingerprint to whitelist");
            }
        }
    }
}
