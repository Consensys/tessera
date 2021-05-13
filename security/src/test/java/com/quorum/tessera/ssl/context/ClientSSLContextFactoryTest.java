package com.quorum.tessera.ssl.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.net.ssl.SSLContext;
import org.junit.Before;
import org.junit.Test;

public class ClientSSLContextFactoryTest {

  private EnvironmentVariableProvider envVarProvider;

  private String envVarPrefix = "PREFIX";

  @Before
  public void setUp() {
    envVarProvider = EnvironmentVariableProviderFactory.load().create();
    when(envVarProvider.getEnvAsCharArray(anyString())).thenReturn(null);
  }

  @Test
  public void createFromConfig() throws Exception {
    SslConfig config = mock(SslConfig.class);

    Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
    when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
    when(config.getClientKeyStore()).thenReturn(keyStore);
    when(config.getClientKeyStorePassword()).thenReturn("password".toCharArray());
    when(config.getClientTrustStore()).thenReturn(trustStore);
    when(config.getClientTrustStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownServersFile()).thenReturn(knownServers);

    SSLContext result = ClientSSLContextFactory.create().from("localhost", config);

    assertThat(result).isNotNull();
  }

  @Test
  public void createFromConfigWithDefaultKnownServers() throws URISyntaxException {
    SslConfig config = mock(SslConfig.class);

    Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());

    when(config.getClientTrustMode()).thenReturn(SslTrustMode.TOFU);
    when(config.getClientKeyStore()).thenReturn(keyStore);
    when(config.getClientKeyStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownServersFile()).thenReturn(null);

    SSLContext result = ClientSSLContextFactory.create().from("localhost", config);

    assertThat(result)
        .isNotNull()
        .extracting("contextSpi")
        .isNotNull()
        .extracting("trustManager")
        .isNotNull()
        .extracting("tm")
        .isNotNull()
        .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class)
        .extracting("knownHostsFile")
        .asList()
        .first()
        .isEqualTo(Paths.get("knownServers"));
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInConfigReturnsConfigValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(password);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(String.valueOf(result)).isEqualTo(String.valueOf(password));
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(password);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(password);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientKeyStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getClientKeyStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test
  public void getClientKeyStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test
  public void
      getClientKeyStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInConfigReturnsConfigValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(password);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(password);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(password);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientTrustStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getClientTrustStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test
  public void getClientTrustStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test
  public void
      getClientTrustStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {
    ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(envVarProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(envVarProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = factory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
  }

  @Test(expected = TesseraSecurityException.class)
  public void securityExceptionsAreThrownAsTesseraException() throws Exception {
    SslConfig config = mock(SslConfig.class);

    Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
    when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
    when(config.getClientKeyStore()).thenReturn(keyStore);
    when(config.getClientKeyStorePassword()).thenReturn("bogus".toCharArray());
    when(config.getClientTrustStore()).thenReturn(trustStore);
    when(config.getClientTrustStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownServersFile()).thenReturn(knownServers);

    ClientSSLContextFactory.create().from("localhost", config);
  }
}
