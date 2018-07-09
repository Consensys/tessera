package com.github.tessera.ssl.context;

import com.github.tessera.config.SslConfig;
import com.github.tessera.config.SslTrustMode;
import com.github.tessera.ssl.exception.TesseraSecurityException;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerSSLContextFactoryTest {

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
        when(config.getKnownClientsFile()).thenReturn(knownServers);

        SSLContext result = ServerSSLContextFactory.create().from(config);

        assertThat(result).isNotNull();

    }

    @Test(expected = TesseraSecurityException.class)
    public void securityExceptionsAreThrownAsTesseraException() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getServerTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getServerKeyStore()).thenReturn(keyStore);
        when(config.getServerKeyStorePassword()).thenReturn("bogus");
        when(config.getServerTrustStore()).thenReturn(trustStore);
        when(config.getServerTrustStorePassword()).thenReturn("password");
        when(config.getKnownClientsFile()).thenReturn(knownServers);

        ServerSSLContextFactory.create().from(config);

    }
}
