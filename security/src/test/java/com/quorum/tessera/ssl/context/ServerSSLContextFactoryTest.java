package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerSSLContextFactoryTest {

    private EnvironmentVariableProvider envVarProvider;
    
    @Before
    public void setUp() {
        envVarProvider = EnvironmentVariableProviderFactory.load().create();
        when(envVarProvider.getEnv(EnvironmentVariables.serverKeyStorePwd)).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.serverTrustStorePwd)).thenReturn(null);
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
        when(config.getKnownClientsFile()).thenReturn(knownServers);

        SSLContext result = ServerSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull();

    }

    @Test
    public void createFromConfigWithDefaultKnownClients() throws URISyntaxException {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());

        when(config.getServerTrustMode()).thenReturn(SslTrustMode.TOFU);
        when(config.getServerKeyStore()).thenReturn(keyStore);
        when(config.getServerKeyStorePassword()).thenReturn("password");
        when(config.getKnownClientsFile()).thenReturn(null);

        SSLContext result = ServerSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class)
            .extracting("knownHostsFile").asList().first().isEqualTo(Paths.get("knownClients"));
    }

    @Test
    public void getServerKeyStorePasswordOnlyConfigSetReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerKeyStorePassword()).thenReturn(password);

        when(envVarProvider.getEnv(EnvironmentVariables.serverKeyStorePwd)).thenReturn(null);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerKeyStorePasswordOnlyEnvSetReturnsEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerKeyStorePassword()).thenReturn(null);

        when(envVarProvider.getEnv(EnvironmentVariables.serverKeyStorePwd)).thenReturn(password);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerKeyStorePasswordEnvAndConfigSetReturnsEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configPassword = "config";
        String envPassword = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerKeyStorePassword()).thenReturn(configPassword);

        when(envVarProvider.getEnv(EnvironmentVariables.serverKeyStorePwd)).thenReturn(envPassword);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(envPassword);
    }

    @Test
    public void getServerTrustStorePasswordOnlyConfigSetReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerTrustStorePassword()).thenReturn(password);

        when(envVarProvider.getEnv(EnvironmentVariables.serverTrustStorePwd)).thenReturn(null);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerTrustStorePasswordOnlyEnvSetReturnsEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerTrustStorePassword()).thenReturn(null);

        when(envVarProvider.getEnv(EnvironmentVariables.serverTrustStorePwd)).thenReturn(password);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerTrustStorePasswordEnvAndConfigSetReturnsEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configPassword = "config";
        String envPassword = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getServerTrustStorePassword()).thenReturn(configPassword);

        when(envVarProvider.getEnv(EnvironmentVariables.serverTrustStorePwd)).thenReturn(envPassword);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(envPassword);
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

        ServerSSLContextFactory.create().from("localhost",config);

    }
}
