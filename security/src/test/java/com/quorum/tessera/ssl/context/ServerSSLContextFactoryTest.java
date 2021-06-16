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

public class ServerSSLContextFactoryTest {

  private EnvironmentVariableProvider environmentVariableProvider;

  private String envVarPrefix = "PREFIX";

  private ServerSSLContextFactoryImpl serverSSLContextFactory;

  @Before
  public void beforeTest() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    serverSSLContextFactory = new ServerSSLContextFactoryImpl(environmentVariableProvider);
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
    when(config.getServerTrustMode()).thenReturn(SslTrustMode.CA);
    when(config.getServerKeyStore()).thenReturn(keyStore);
    when(config.getServerKeyStorePassword()).thenReturn("password".toCharArray());
    when(config.getServerTrustStore()).thenReturn(trustStore);
    when(config.getServerTrustStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownClientsFile()).thenReturn(knownServers);

    SSLContext result = serverSSLContextFactory.from("localhost", config);

    assertThat(result).isNotNull();
  }

  @Test
  public void createFromConfigWithDefaultKnownClients() throws URISyntaxException {
    SslConfig config = mock(SslConfig.class);

    Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());

    when(config.getServerTrustMode()).thenReturn(SslTrustMode.TOFU);
    when(config.getServerKeyStore()).thenReturn(keyStore);
    when(config.getServerKeyStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownClientsFile()).thenReturn(null);

    SSLContext result = serverSSLContextFactory.from("localhost", config);

    assertThat(result)
        .isNotNull()
        .extracting("contextSpi.trustManager.tm")
        .isNotNull()
        .isExactlyInstanceOf(TrustOnFirstUseManager.class);

    assertThat(result)
        .extracting("contextSpi.trustManager.tm.knownHostsFile")
        .isNotNull()
        .isInstanceOf(Path.class)
        .isEqualTo(Paths.get("knownClients"));

    verify(environmentVariableProvider)
        .getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test
  public void getServerKeyStorePasswordOnlySetInConfigReturnsConfigValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getServerKeyStorePassword()).thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getServerKeyStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider).getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD);
  }

  @Test
  public void getServerKeyStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {

    char[] password = "password".toCharArray();
    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(password);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(
            envVarPrefix.concat("_").concat(EnvironmentVariables.SERVER_KEYSTORE_PWD));
  }

  @Test
  public void getServerKeyStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getServerKeyStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD);
  }

  @Test
  public void getServerKeyStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD);
  }

  @Test
  public void
      getServerKeyStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerKeyStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerKeyStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_KEYSTORE_PWD);
  }

  @Test
  public void getServerTrustStorePasswordOnlySetInConfigReturnsConfigValue() {

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
  }

  @Test
  public void getServerTrustStorePasswordOnlySetInGlobalEnvReturnsGlobalEnvValue() {

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(password);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);
    verify(environmentVariableProvider)
        .getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test
  public void getServerTrustStorePasswordOnlySetInPrefixedEnvReturnsPrefixedEnvValue() {

    char[] password = "password".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(password);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(password);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test
  public void getServerTrustStorePasswordSetInConfigAndGlobalEnvReturnsConfigValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(configVal);
  }

  @Test
  public void getServerTrustStorePasswordSetInConfigAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] prefixedEnvVal = "env".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test
  public void getServerTrustStorePasswordSetInGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(null);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);
    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test
  public void
      getServerTrustStorePasswordSetInConfigAndGlobalEnvAndPrefixedEnvReturnsPrefixedEnvValue() {

    char[] configVal = "config".toCharArray();
    char[] globalEnvVal = "global".toCharArray();
    char[] prefixedEnvVal = "prefixed".toCharArray();

    SslConfig sslConfig = mock(SslConfig.class);
    when(sslConfig.getEnvironmentVariablePrefix()).thenReturn(envVarPrefix);

    when(sslConfig.getServerTrustStorePassword()).thenReturn(configVal);
    when(environmentVariableProvider.getEnvAsCharArray(EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(globalEnvVal);
    when(environmentVariableProvider.getEnvAsCharArray(
            envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD))
        .thenReturn(prefixedEnvVal);

    char[] result = serverSSLContextFactory.getServerTrustStorePassword(sslConfig);

    assertThat(result).isEqualTo(prefixedEnvVal);

    verify(environmentVariableProvider)
        .getEnvAsCharArray(envVarPrefix + "_" + EnvironmentVariables.SERVER_TRUSTSTORE_PWD);
  }

  @Test(expected = TesseraSecurityException.class)
  public void securityExceptionsAreThrownAsTesseraException() throws Exception {
    SslConfig config = mock(SslConfig.class);

    Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
    Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
    when(config.getServerTrustMode()).thenReturn(SslTrustMode.CA);
    when(config.getServerKeyStore()).thenReturn(keyStore);
    when(config.getServerKeyStorePassword()).thenReturn("bogus".toCharArray());
    when(config.getServerTrustStore()).thenReturn(trustStore);
    when(config.getServerTrustStorePassword()).thenReturn("password".toCharArray());
    when(config.getKnownClientsFile()).thenReturn(knownServers);

    serverSSLContextFactory.from("localhost", config);
  }

  @Test
  public void create() {
    assertThat(ServerSSLContextFactory.create()).isNotNull();
  }
}
