package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
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

import java.util.Objects;

import static com.quorum.tessera.config.util.EnvironmentVariables.*;

class HashicorpKeyVaultServiceFactoryUtil {

    SslConfiguration configureSsl(HashicorpKeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider) {
        if(keyVaultConfig.getTlsKeyStorePath() != null && keyVaultConfig.getTlsTrustStorePath() != null) {

            Resource clientKeyStore = new FileSystemResource(keyVaultConfig.getTlsKeyStorePath().toFile());
            Resource clientTrustStore = new FileSystemResource(keyVaultConfig.getTlsTrustStorePath().toFile());

            SslConfiguration.KeyStoreConfiguration keyStoreConfiguration = SslConfiguration.KeyStoreConfiguration.of(
                clientKeyStore,
                envProvider.getEnvAsCharArray(HASHICORP_CLIENT_KEYSTORE_PWD)
            );

            SslConfiguration.KeyStoreConfiguration trustStoreConfiguration = SslConfiguration.KeyStoreConfiguration.of(
                clientTrustStore,
                envProvider.getEnvAsCharArray(HASHICORP_CLIENT_TRUSTSTORE_PWD)
            );

            return new SslConfiguration(keyStoreConfiguration, trustStoreConfiguration);

        } else if (keyVaultConfig.getTlsTrustStorePath() != null) {

            Resource clientTrustStore = new FileSystemResource(keyVaultConfig.getTlsTrustStorePath().toFile());

            return SslConfiguration.forTrustStore(clientTrustStore, envProvider.getEnvAsCharArray(HASHICORP_CLIENT_TRUSTSTORE_PWD));

        } else {
            return SslConfiguration.unconfigured();
        }
    }

    ClientHttpRequestFactory createClientHttpRequestFactory(ClientOptions clientOptions, SslConfiguration sslConfiguration) {
        return ClientHttpRequestFactoryFactory.create(clientOptions, sslConfiguration);
    }

    ClientAuthentication configureClientAuthentication(HashicorpKeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider, ClientHttpRequestFactory clientHttpRequestFactory, VaultEndpoint vaultEndpoint) {

        final String roleId = envProvider.getEnv(HASHICORP_ROLE_ID);
        final String secretId = envProvider.getEnv(HASHICORP_SECRET_ID);
        final String authToken = envProvider.getEnv(HASHICORP_TOKEN);

        if(roleId != null && secretId != null) {

            AppRoleAuthenticationOptions appRoleAuthenticationOptions = AppRoleAuthenticationOptions.builder()
                .path(keyVaultConfig.getApprolePath())
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId))
                .build();

            RestOperations restOperations = VaultClients.createRestTemplate(vaultEndpoint, clientHttpRequestFactory);

            return new AppRoleAuthentication(appRoleAuthenticationOptions, restOperations);

        } else if (Objects.isNull(roleId) != Objects.isNull(secretId)) {

            throw new HashicorpCredentialNotSetException("Both " + HASHICORP_ROLE_ID + " and " + HASHICORP_SECRET_ID + " environment variables must be set to use the AppRole authentication method");

        } else if (authToken == null){

            throw new HashicorpCredentialNotSetException("Both " + HASHICORP_ROLE_ID + " and " + HASHICORP_SECRET_ID + " environment variables must be set to use the AppRole authentication method.  Alternatively set " + HASHICORP_TOKEN + " to authenticate using the Token method");
        }

        return new TokenAuthentication(authToken);
    }
}
