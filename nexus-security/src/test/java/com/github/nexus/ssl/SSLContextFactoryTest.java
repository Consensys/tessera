package com.github.nexus.ssl;

import com.github.nexus.config.SslConfig;
import com.github.nexus.config.SslTrustMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.net.ssl.SSLContext;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SSLContextFactoryTest {

    public SSLContextFactoryTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createFromConfig() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getServerTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getServerKeyStore()).thenReturn(keyStore);
        when(config.getServerKeyStorePassword()).thenReturn("password");
        when(config.getServerTrustStore()).thenReturn(trustStore);
        when(config.getServerTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        SSLContext result = SSLContextFactory.create().from(config);

        assertThat(result).isNotNull();

    }

    @Test
    public void createFromConfigNoTrustMode() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());

        when(config.getServerKeyStore()).thenReturn(keyStore);
        when(config.getServerKeyStorePassword()).thenReturn("password");
        when(config.getServerTrustStore()).thenReturn(trustStore);
        when(config.getServerTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        SSLContext result = SSLContextFactory.create().from(config);

        assertThat(result).isNotNull();

    }

    @Test(expected = NexusSecurityException.class)
    public void securityExceptionsAreThrownAsNexusException() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getServerTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getServerKeyStore()).thenReturn(keyStore);
        when(config.getServerKeyStorePassword()).thenReturn("bogus");
        when(config.getServerTrustStore()).thenReturn(trustStore);
        when(config.getServerTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        SSLContextFactory.create().from(config);

    }
}
