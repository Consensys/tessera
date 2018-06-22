package com.github.nexus.ssl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.*;
import static org.mockito.Mockito.*;

public class TrustOnFirstUseManagerTest {

    TrustOnFirstUseManager trustManager;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    File knownHosts;

    @Mock
    X509Certificate certificate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        knownHosts = new File(tmpDir.getRoot(), "knownHosts");
    }

    @After
    public void tearDown(){
        verifyNoMoreInteractions(certificate);
    }

    @Test
    public void testAddFingerPrintToKnownHostsList() throws CertificateException, IOException {
        trustManager = new TrustOnFirstUseManager(knownHosts);
        when(certificate.getEncoded()).thenReturn("certificate".getBytes());
        trustManager.checkServerTrusted(new X509Certificate[]{certificate}, "s");
        trustManager.checkClientTrusted(new X509Certificate[]{certificate}, "s");
        verify(certificate, times(2)).getEncoded();
    }

    @Test
    public void testAddFingerPrintFailedToWrite() throws CertificateException, IOException {
        File notWritable = mock(File.class);
        when(notWritable.canWrite()).thenReturn(false);

        trustManager = new TrustOnFirstUseManager(notWritable);

        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn("certificate".getBytes());

        try {
            trustManager.checkServerTrusted(new X509Certificate[]{certificate}, "s");
            trustManager.checkClientTrusted(new X509Certificate[]{certificate}, "s");
        }
        catch(Exception ex){
            assertThat(ex).isInstanceOf(CertificateException.class);
        }
    }

    @Test
    public void testGetAcceptIssuers() throws IOException {
        trustManager = new TrustOnFirstUseManager(knownHosts);
        assertThat(trustManager.getAcceptedIssuers()).isEmpty();
    }
}
