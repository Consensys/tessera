package com.quorum.tessera.socket;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.SocketFactory;
import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HttpProxyFactoryTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void secureHttpProxy() throws Exception {

        final URI uri = new URI("http://bogus.com");

        final File tmpFile = new File(tmpDir.getRoot(), "keystores");

        final ServerConfig configuration = mock(ServerConfig.class);
        when(configuration.getBindingAddress()).thenReturn(uri.toString());
        when(configuration.getServerUri()).thenReturn(uri);
        when(configuration.isSsl()).thenReturn(true);
        SslConfig sslConfig = mock(SslConfig.class);
        
        when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.STRICT);
        when(sslConfig.getClientKeyStore()).thenReturn(tmpFile.toPath());
        when(sslConfig.getClientKeyStorePassword()).thenReturn("somepwd");
        when(sslConfig.getClientTrustMode()).thenReturn(SslTrustMode.NONE);
        when(sslConfig.getClientTrustStore()).thenReturn(tmpFile.toPath());
        when(sslConfig.getClientTrustStorePassword()).thenReturn("somepwd");

        when(sslConfig.getKnownServersFile()).thenReturn(tmpFile.toPath());
        
        when(configuration.getSslConfig()).thenReturn(sslConfig);

        HttpProxyFactory proxyFactory = new HttpProxyFactory(configuration);
        HttpProxy proxy = proxyFactory.create();

        assertThat(proxy)
                .isNotNull()
                .extracting("socketFactory")
                .extracting("class")
                .containsExactly(SSLSocketFactoryImpl.class);

    }

    @Test
    public void insecureHttpProxy() throws Exception {
        final URI uri = new URI("http://bogus.com");

        final ServerConfig configuration = mock(ServerConfig.class);
        when(configuration.getServerUri()).thenReturn(uri);
        when(configuration.isSsl()).thenReturn(false);

        final HttpProxyFactory proxyFactory = new HttpProxyFactory(configuration);
        final HttpProxy proxy = proxyFactory.create();

        assertThat(proxy)
                .isNotNull()
                .extracting("socketFactory")
                .containsExactly(SocketFactory.getDefault());

    }

}
