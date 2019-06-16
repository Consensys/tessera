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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerSSLContextFactoryTest {

    private EnvironmentVariableProvider envVarProvider;

    private String envVarPrefix = "PREFIX";
    
    @Before
    public void setUp() {
        envVarProvider = EnvironmentVariableProviderFactory.load().create();
        when(envVarProvider.getEnv(anyString())).thenReturn(null);
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
    public void getServerKeyStorePasswordOnlySetInConfigReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";
        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(password);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerKeyStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";
        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(password);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerKeyStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";
        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(password);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerKeyStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String globalEnvVal = "env";

        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(configVal);
    }

    @Test
    public void getServerKeyStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String prefixedEnvVal = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
    }

    @Test
    public void getServerKeyStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String globalEnvVal = "global";
        String prefixedEnvVal = "prefixed";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
    }

    @Test
    public void getServerKeyStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String globalEnvVal = "global";
        String prefixedEnvVal = "prefixed";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
    }

    @Test
    public void getServerTrustStorePasswordOnlySetInConfigReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(password);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerTrustStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(password);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerTrustStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(password);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getServerTrustStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String globalEnvVal = "env";

        SslConfig sslConfig = mock(SslConfig.class);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(configVal);
    }

    @Test
    public void getServerTrustStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String prefixedEnvVal = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(null);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
    }

    @Test
    public void getServerTrustStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String globalEnvVal = "global";
        String prefixedEnvVal = "prefixed";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
    }

    @Test
    public void getServerTrustStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
        ServerSSLContextFactoryImpl factory = new ServerSSLContextFactoryImpl();

        String configVal = "config";
        String globalEnvVal = "global";
        String prefixedEnvVal = "prefixed";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

        when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
        when(envVarProvider.getEnv(EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(globalEnvVal);
        when(envVarProvider.getEnv(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD)).thenReturn(prefixedEnvVal);

        String result = factory.getServerTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(prefixedEnvVal);
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
