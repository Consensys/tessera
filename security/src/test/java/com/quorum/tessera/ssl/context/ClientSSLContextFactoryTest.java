package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
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

        SSLContext result = ClientSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull();

    }

    @Test
    public void createFromConfigWithDefaultKnownServers() throws URISyntaxException {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());

        when(config.getClientTrustMode()).thenReturn(SslTrustMode.TOFU);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(null);

        SSLContext result = ClientSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class)
            .extracting("knownHostsFile").asList().first().isEqualTo(Paths.get("knownServers"));
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

        ClientSSLContextFactory.create().from("localhost",config);

    }
}
