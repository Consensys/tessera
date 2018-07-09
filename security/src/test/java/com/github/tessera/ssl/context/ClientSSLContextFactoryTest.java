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

public class ClientSSLContextFactoryTest {


    @Test
    public void createFromConfig() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("password");
        when(config.getClientTrustStore()).thenReturn(trustStore);
        when(config.getClientTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        SSLContext result = ClientSSLContextFactory.create().from(config);

        assertThat(result).isNotNull();

    }

    @Test(expected = TesseraSecurityException.class)
    public void securityExceptionsAreThrownAsTesseraException() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("bogus");
        when(config.getClientTrustStore()).thenReturn(trustStore);
        when(config.getClientTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        ClientSSLContextFactory.create().from(config);

    }
}
