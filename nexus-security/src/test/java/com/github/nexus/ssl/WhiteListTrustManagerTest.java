package com.github.nexus.ssl;

import com.github.nexus.ssl.util.CertificateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class WhiteListTrustManagerTest {

    private WhiteListTrustManager trustManager;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    File knownHosts;

    @Mock
    X509Certificate certificate;


    @Before
    public void setUp() throws IOException, CertificateException {
        MockitoAnnotations.initMocks(this);
        when(certificate.getEncoded()).thenReturn("fingerprint".getBytes());
        knownHosts = new File(tmpDir.getRoot(), "knownHosts");
        knownHosts.createNewFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(knownHosts, true)))
        {
            writer.write("somefingerprint");
            writer.newLine();
            writer.write(CertificateUtil.generateFingerprint(certificate));
            writer.newLine();
        }

        trustManager = new WhiteListTrustManager(knownHosts);

    }

    @After
    public void tearDown(){
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
        when(certificate.getEncoded()).thenReturn("some-other-fingerprint".getBytes());
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
