package com.quorum.tessera.key.vault.hashicorp;

import static com.quorum.tessera.config.util.EnvironmentVariables.*;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.ClientHttpRequestFactoryFactory;
import org.springframework.vault.support.ClientOptions;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.web.client.RestOperations;

class HashicorpKeyVaultServiceFactoryUtil {

  SslConfiguration configureSsl(
      KeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider) {
    if (keyVaultConfig.hasProperty("tlsKeyStorePath", "tlsTrustStorePath")) {

      Path tlsKeyStorePath = keyVaultConfig.getProperty("tlsKeyStorePath").map(Paths::get).get();
      Path tlsTrustStorePath =
          keyVaultConfig.getProperty("tlsTrustStorePath").map(Paths::get).get();

      Resource clientKeyStore = new FileSystemResource(tlsKeyStorePath.toFile());
      Resource clientTrustStore = new FileSystemResource(tlsTrustStorePath.toFile());

      SslConfiguration.KeyStoreConfiguration keyStoreConfiguration =
          SslConfiguration.KeyStoreConfiguration.of(
              clientKeyStore, envProvider.getEnvAsCharArray(HASHICORP_CLIENT_KEYSTORE_PWD));

      SslConfiguration.KeyStoreConfiguration trustStoreConfiguration =
          SslConfiguration.KeyStoreConfiguration.of(
              clientTrustStore, envProvider.getEnvAsCharArray(HASHICORP_CLIENT_TRUSTSTORE_PWD));

      return new SslConfiguration(keyStoreConfiguration, trustStoreConfiguration);

    } else if (keyVaultConfig.hasProperty("tlsTrustStorePath")) {

      Path tlsTrustStorePath =
          keyVaultConfig.getProperty("tlsTrustStorePath").map(Paths::get).get();
      Resource clientTrustStore = new FileSystemResource(tlsTrustStorePath.toFile());

      return SslConfiguration.forTrustStore(
          clientTrustStore, envProvider.getEnvAsCharArray(HASHICORP_CLIENT_TRUSTSTORE_PWD));

    } else {
      return SslConfiguration.unconfigured();
    }
  }

  ClientHttpRequestFactory createClientHttpRequestFactory(
      ClientOptions clientOptions, SslConfiguration sslConfiguration) {
    return ClientHttpRequestFactoryFactory.create(clientOptions, sslConfiguration);
  }

  ClientAuthentication configureClientAuthentication(
      KeyVaultConfig keyVaultConfig,
      EnvironmentVariableProvider envProvider,
      ClientHttpRequestFactory clientHttpRequestFactory,
      VaultEndpoint vaultEndpoint) {

    final String roleId = envProvider.getEnv(HASHICORP_ROLE_ID);
    final String secretId = envProvider.getEnv(HASHICORP_SECRET_ID);
    final String authToken = envProvider.getEnv(HASHICORP_TOKEN);

    if (roleId != null && secretId != null) {

      AppRoleAuthenticationOptions appRoleAuthenticationOptions =
          AppRoleAuthenticationOptions.builder()
              .path(keyVaultConfig.getProperty("approlePath").get())
              .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
              .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId))
              .build();

      RestOperations restOperations =
          VaultClients.createRestTemplate(vaultEndpoint, clientHttpRequestFactory);

      return new AppRoleAuthentication(appRoleAuthenticationOptions, restOperations);

    } else if (Objects.isNull(roleId) != Objects.isNull(secretId)) {

      throw new HashicorpCredentialNotSetException(
          "Both "
              + HASHICORP_ROLE_ID
              + " and "
              + HASHICORP_SECRET_ID
              + " environment variables must be set to use the AppRole authentication method");

    } else if (authToken == null) {

      throw new HashicorpCredentialNotSetException(
          "Both "
              + HASHICORP_ROLE_ID
              + " and "
              + HASHICORP_SECRET_ID
              + " environment variables must be set to use the AppRole authentication method.  Alternatively set "
              + HASHICORP_TOKEN
              + " to authenticate using the Token method");
    }

    return new TokenAuthentication(authToken);
  }
}
