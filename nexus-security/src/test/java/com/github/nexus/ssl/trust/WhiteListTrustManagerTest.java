package com.github.nexus.ssl.trust;

import com.github.nexus.ssl.util.CertificateUtil;
import org.junit.*;
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

    private static TemporaryFolder tmpDirDelegate;

    File knownHosts;

    @Mock
    X509Certificate certificate;


    @Before
    public void setUp() throws IOException, CertificateException {
        MockitoAnnotations.initMocks(this);
        when(certificate.getEncoded()).thenReturn("thumbprint".getBytes());
        knownHosts = new File(tmpDir.getRoot(), "knownHosts");
        knownHosts.createNewFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(knownHosts, true)))
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
        tmpDirDelegate = tmpDir;
        assertThat(tmpDirDelegate.getRoot().exists()).isTrue();
    }

    @AfterClass
    public static void tearDown() {
        assertThat(tmpDirDelegate.getRoot().exists()).isFalse();
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
