package com.github.nexus.ssl.util;

import com.github.nexus.ssl.util.CertificateUtil;
import org.junit.Test;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateUtilTest {


    @Test
    public void testGenerateThumbPrint() throws CertificateException {

        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn("some".getBytes());

        assertThat(CertificateUtil.generateThumbPrint(certificate))
            .isEqualTo("eb875812858d27b22cb2b75f992dffadc1b05c66");

    }

    @Test
    public void testGenerateThrowException(){
        X509Certificate certificate = mock(X509Certificate.class);
        try {
            CertificateUtil.generateThumbPrint(certificate);
            failBecauseExceptionWasNotThrown(Exception.class);

        } catch (Exception ex) {
            assertThat(ex)
                .isInstanceOf(CertificateException.class)
                .hasMessage("Cannot generate thumbprint for this certificate");
        }
    }
}
