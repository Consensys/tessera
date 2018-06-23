package com.github.nexus.ssl;

import com.github.nexus.ssl.trust.TrustOnFirstUseManager;
import com.github.nexus.ssl.trust.WhiteListTrustManager;
import org.junit.Before;
import org.junit.Test;
import sun.security.ssl.SSLContextImpl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SSLContextBuilderTest {

    private SSLContextBuilder sslContextBuilder;

    File file = mock(File.class);

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        sslContextBuilder = SSLContextBuilder.createBuilder(
            "/Users/namtruong/workspace/Nexus/nexus-security/src/test/resources/test-keystore",
            "quorum",
            "/Users/namtruong/workspace/Nexus/nexus-security/src/test/resources/test-truststore",
            "quorum");
    }

    @Test
    public void testBuildForTrustOnFirstUse() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, IllegalAccessException, NoSuchFieldException {

        final SSLContext sslContext = sslContextBuilder.forTrustOnFirstUse(file).build();

        Object trustManager = useReflectionToRetrieveTrustManagerFromSSLContext(sslContext);

        assertThat(trustManager).isInstanceOf(TrustOnFirstUseManager.class);

    }

    @Test
    public void testBuildForWhiteList() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, NoSuchFieldException, IllegalAccessException {
        final SSLContext sslContext = sslContextBuilder.forWhiteList(file).build();

        Object trustManager = useReflectionToRetrieveTrustManagerFromSSLContext(sslContext);

        assertThat(trustManager).isInstanceOf(WhiteListTrustManager.class);


    }

    @Test
    public void testBuildForCASignedCertificates() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, NoSuchFieldException, IllegalAccessException {

        final SSLContext sslContext = sslContextBuilder.forCASignedCertificates().build();

        assertThat(sslContext).isNotNull();

    }

    @Test
    public void testBuildForAllCertificates() throws KeyManagementException, NoSuchFieldException, IllegalAccessException {

        final SSLContext sslContext = sslContextBuilder.forAllCertificates().build();

        Object trustManager = useReflectionToRetrieveTrustManagerFromSSLContext(sslContext);

        assertThat(trustManager).isInstanceOf(TrustManager.class);
    }


    //TODO This is not ideal
    private Object useReflectionToRetrieveTrustManagerFromSSLContext(final SSLContext sslContext) throws NoSuchFieldException, IllegalAccessException {

        Field contextSpiField = sslContext.getClass().getDeclaredField("contextSpi");
        contextSpiField.setAccessible(true);
        Object openSSLContextImplObj = contextSpiField.get(sslContext);

        Field trustManagerWrapperField = SSLContextImpl.class.getDeclaredField("trustManager");
        trustManagerWrapperField.setAccessible(true);
        Object trustManagerWrapper = trustManagerWrapperField.get(openSSLContextImplObj);

        Field trustManagerField = trustManagerWrapper.getClass().getDeclaredField("tm");
        trustManagerField.setAccessible(true);
        Object trustManager = trustManagerField.get(trustManagerWrapper);

        return trustManager;
    }
}
