package com.github.nexus.ssl.util;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface CertificateUtil {

    default String thumbPrint(final X509Certificate certificate) throws CertificateException {
        try {
            final byte[] encoded = certificate.getEncoded();
            return DatatypeConverter.printHexBinary(
                MessageDigest.getInstance("SHA-1").digest(encoded)).toLowerCase();
        } catch (Exception ex) {
            throw new CertificateException("Cannot generate thumbprint for this certificate. Cause by ", ex);
        }
    }

    static CertificateUtil create() {
        return new CertificateUtil() {
        };
    }
}
