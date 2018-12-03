package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.Vault;
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
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        return null;
    }

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider, KeyVaultClientFactory keyVaultClientFactory) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);

        if(!(keyVaultClientFactory instanceof HashicorpKeyVaultClientFactory)) {
            throw new HashicorpVaultException("Incorrect KeyVaultClientFactoryType passed to HashicorpKeyVaultServiceFactory");
        }

        String roleId = envProvider.getEnv(roleIdEnvVar);
        String secretId = envProvider.getEnv(secretIdEnvVar);
        String authToken = envProvider.getEnv(authTokenEnvVar);

        if(roleId == null && secretId == null && authToken == null) {
            throw new HashicorpCredentialNotSetException("Environment variables must be set to authenticate with Hashicorp Vault.  Set the " + roleIdEnvVar + " and " + secretIdEnvVar + " environment variables if using the AppRole authentication method.  Set the " + authTokenEnvVar + " environment variable if using another authentication method.");
        }
        else if(isOnlyOneInputNull(roleId, secretId)) {
            throw new HashicorpCredentialNotSetException("Only one of the " + roleIdEnvVar + " and " + secretIdEnvVar + " environment variables to authenticate with Hashicorp Vault using the AppRole method has been set");
        }

        HashicorpKeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .map(KeyConfiguration::getHashicorpKeyVaultConfig)
            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Hashicorp Vault connection but no Vault configuration provided")));

        Vault vault = ((HashicorpKeyVaultClientFactory) keyVaultClientFactory)
            .init(keyVaultConfig, new VaultConfigFactory(), new SslConfigFactory())
            .login(roleId, secretId, authToken)
            .create();

//        HashicorpKeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
//            .map(KeyConfiguration::getHashicorpKeyVaultConfig)
//            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Hashicorp Vault connection but no Vault configuration provided")));
//
//        VaultConfig vaultConfig = new VaultConfig()
//            .address(keyVaultConfig.getUrl());
//
//        if(keyVaultConfig.getTlsCertificatePath() != null) {
//            SslConfig vaultSslConfig = new SslConfig();
//                VaultCallback.execute(
//                    () -> vaultSslConfig
//                        .pemFile(keyVaultConfig.getTlsCertificatePath().toFile())
//                        .build()
//                );
//
//            vaultConfig.sslConfig(vaultSslConfig);
//        }
//
//        VaultCallback.execute(vaultConfig::build);
//
//        final Vault vault = new Vault(vaultConfig);
//
//        String token;
//
//        if(roleId != null && secretId != null) {
//            AuthResponse loginResponse = VaultCallback.execute(() -> vault.auth().loginByAppRole("approle", roleId, secretId));
//            token = loginResponse.getAuthClientToken();
//        } else {
//            token = authToken;
//        }
//
//        vaultConfig.token(token);

        return new HashicorpKeyVaultService(keyVaultConfig, vault);
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }

    private boolean isOnlyOneInputNull(Object obj1, Object obj2) {
        return Objects.isNull(obj1) ^ Objects.isNull(obj2);
    }
}
