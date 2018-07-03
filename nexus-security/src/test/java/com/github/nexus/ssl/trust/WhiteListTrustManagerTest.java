package com.github.nexus.ssl.trust;

import com.github.nexus.ssl.util.CertificateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class WhiteListTrustManagerTest {

    private WhiteListTrustManager trustManager;

    Path knownHosts;

    @Mock
    X509Certificate certificate;


    @Before
    public void setUp() throws IOException, CertificateException {
        MockitoAnnotations.initMocks(this);
        when(certificate.getEncoded()).thenReturn("thumbprint".getBytes());
        knownHosts = Files.createTempFile("test", "knownHosts");

        try (BufferedWriter writer = Files.newBufferedWriter(knownHosts, StandardOpenOption.APPEND))
        {
            writer.write("somethumbprint");
            writer.newLine();
            writer.write(CertificateUtil.create().thumbPrint(certificate));
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

        trustManager.checkServerTrusted(new X509Certificate[]{certificate},"str");
        trustManager.checkClientTrusted(new X509Certificate[]{certificate},"str");

        verify(certificate, times(3)).getEncoded();

    }

    @Test
    public void testCertificatesNotInWhiteList() throws CertificateException {
        when(certificate.getEncoded()).thenReturn("some-other-thumbprint".getBytes());
        try {
            trustManager.checkClientTrusted(new X509Certificate[]{certificate}, "str");
            failBecauseExceptionWasNotThrown(Exception.class);
        }
        catch (Exception ex){
            assertThat(ex)
                .isInstanceOf(CertificateException.class)
                .hasMessage("Connections not allowed");
        }
        verify(certificate, times(2)).getEncoded();
    }

    @Test
    public void testGetAcceptIssuers() throws CertificateEncodingException {
        assertThat(trustManager.getAcceptedIssuers()).isEmpty();
        verify(certificate).getEncoded();
    }

}
