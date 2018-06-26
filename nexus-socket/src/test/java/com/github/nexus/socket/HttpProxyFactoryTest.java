
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.SocketFactory;
import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HttpProxyFactoryTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void secureHttpProxy() throws Exception {

        final URI uri = new URI("http://bogus.com");

        final File tmpFile = new File(tmpDir.getRoot(), "keystores");

        final Configuration configuration = mock(Configuration.class);
        doReturn(uri).when(configuration).uri();
        doReturn("STRICT").when(configuration).tls();
        doReturn(tmpFile.getPath()).when(configuration).clientKeyStore();
        doReturn("somepwd").when(configuration).clientKeyStorePassword();
        doReturn(tmpFile.getPath()).when(configuration).clientTrustStore();
        doReturn("somepwd").when(configuration).clientTrustStorePassword();
        doReturn(tmpFile.getPath()).when(configuration).knownServers();

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

        final Configuration configuration = mock(Configuration.class);
        doReturn(uri).when(configuration).uri();
        doReturn("off").when(configuration).tls();

        final HttpProxyFactory proxyFactory = new HttpProxyFactory(configuration);
        final HttpProxy proxy = proxyFactory.create();

        assertThat(proxy)
            .isNotNull()
            .extracting("socketFactory")
            .containsExactly(SocketFactory.getDefault());

    }

}
