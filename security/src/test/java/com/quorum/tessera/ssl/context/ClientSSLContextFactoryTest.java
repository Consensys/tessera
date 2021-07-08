package com.quorum.tessera.ssl.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariables;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientSSLContextFactoryTest {

  private EnvironmentVariableProvider environmentVariableProvider;

  private String envVarPrefix = "PREFIX";

  private ClientSSLContextFactoryImpl clientSSLContextFactory;

  @Before
  public void beforeTest() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    clientSSLContextFactory = new ClientSSLContextFactoryImpl(environmentVariableProvider);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(environmentVariableProvider);
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

    SSLContext result = clientSSLContextFactory.from("localhost", config);

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

    SSLContext result = clientSSLContextFactory.from("localhost", config);

    assertThat(result).isNotNull();
    assertThat(result)
        .extracting("contextSpi.trustManager.tm")
        .isNotNull()
        .isExactlyInstanceOf(TrustOnFirstUseManager.class);

    assertThat(result)
        .extracting("contextSpi.trustManager.tm.knownHostsFile")
        .isNotNull()
        .isInstanceOf(Path.class)
        .isEqualTo(Paths.get("knownServers"));

    verify(environmentVariableProvider)
        .getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInConfigReturnsConfigValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(password);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(String.valueOf(result)).isEqualTo(String.valueOf(password));
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);

    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(password);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isNotNull().isEqualTo(password);

    verify(environmentVariableProvider).getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD);
  }

  @Test
  public void getClientKeyStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);

    String prefixedEnvionmentVar =
        envVarPrefix.concat("_").concat(EnvironmentVariables.CLIENT_KEYSTORE_PWD);

    when(environmentVariableProvider.getEnvAsCharArray(prefixedEnvionmentVar)).thenReturn(password);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider).getEnvAsCharArray(prefixedEnvionmentVar);
  }

  @Test
  public void getClientKeyStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getClientKeyStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);

    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD);
  }

  @Test
  public void getClientKeyStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD);
  }

  @Test
  public void
      getClientKeyStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientKeyStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_KEYSTORE_PWD);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInConfigReturnsConfigValue() {

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {

    char[] password = "passwordFromEnv".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  @Test
  public void getClientTrustStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {

    char[] password = "passwordFromPrefixedEnvKey".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(password);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  @Test
  public void getClientTrustStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getClientTrustStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  @Test
  public void getClientTrustStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
  }

  @Test
  public void
      getClientTrustStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getClientTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = clientSSLContextFactory.getClientTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.CLIENT_TRUSTSTORE_PWD);
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
