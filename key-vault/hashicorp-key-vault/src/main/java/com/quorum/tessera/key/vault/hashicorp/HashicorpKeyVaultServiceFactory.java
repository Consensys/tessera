package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Objects;
import java.util.Optional;

public class HashicorpKeyVaultServiceFactory implements KeyVaultServiceFactory {

    private final String roleIdEnvVar = "HASHICORP_ROLE_ID";
    private final String secretIdEnvVar = "HASHICORP_SECRET_ID";
    private final String authTokenEnvVar = "HASHICORP_TOKEN";

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider, KeyVaultClientFactory keyVaultClientFactory) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);
        Objects.requireNonNull(keyVaultClientFactory);

        final String roleId = envProvider.getEnv(roleIdEnvVar);
        final String secretId = envProvider.getEnv(secretIdEnvVar);
        final String authToken = envProvider.getEnv(authTokenEnvVar);

        if(roleId == null && secretId == null && authToken == null) {
            throw new HashicorpCredentialNotSetException("Environment variables must be set to authenticate with Hashicorp Vault.  Set the " + roleIdEnvVar + " and " + secretIdEnvVar + " environment variables if using the AppRole authentication method.  Set the " + authTokenEnvVar + " environment variable if using another authentication method.");
        }
        else if(isOnlyOneInputNull(roleId, secretId)) {
            throw new HashicorpCredentialNotSetException("Only one of the " + roleIdEnvVar + " and " + secretIdEnvVar + " environment variables to authenticate with Hashicorp Vault using the AppRole method has been set");
        }

        HashicorpKeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .map(KeyConfiguration::getHashicorpKeyVaultConfig)
            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Hashicorp Vault connection but no Vault configuration provided")));

        if(!(keyVaultClientFactory instanceof HashicorpKeyVaultClientFactory)) {
            throw new HashicorpVaultException("Incorrect KeyVaultClientFactoryType passed to HashicorpKeyVaultServiceFactory");
        }

        HashicorpKeyVaultClientFactory hashicorpClientFactory = (HashicorpKeyVaultClientFactory) keyVaultClientFactory;

        final Vault unauthenticatedVault = hashicorpClientFactory.createUnauthenticatedClient(keyVaultConfig, new VaultConfigFactory(), new SslConfigFactory());

        String token;

        if(roleId != null && secretId != null) {
            try {
                AuthResponse loginResponse = unauthenticatedVault.auth().loginByAppRole(keyVaultConfig.getApprolePath(), roleId, secretId);
                token = loginResponse.getAuthClientToken();
            } catch (VaultException e) {
                throw new HashicorpVaultException("Unable to authenticate using AppRole - " + e.getMessage());
            }

        } else {
            token = authToken;
        }

        final Vault authenticatedVault = hashicorpClientFactory.createAuthenticatedClient(keyVaultConfig, new VaultConfigFactory(), new SslConfigFactory(), token);

        return new HashicorpKeyVaultService(authenticatedVault);
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }

    private boolean isOnlyOneInputNull(Object obj1, Object obj2) {
        return Objects.isNull(obj1) ^ Objects.isNull(obj2);
    }
}
