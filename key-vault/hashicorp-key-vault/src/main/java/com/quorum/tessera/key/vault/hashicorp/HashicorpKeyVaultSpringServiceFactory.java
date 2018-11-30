package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

public class HashicorpKeyVaultSpringServiceFactory implements KeyVaultServiceFactory {

    private final String authTokenEnvVar = "HASHICORP_TOKEN";

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);

        String authToken = envProvider.getEnv(authTokenEnvVar);

        if(authToken == null) {
            throw new HashicorpCredentialNotSetException(authTokenEnvVar + " must be set");
        }

        HashicorpKeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .map(KeyConfiguration::getHashicorpKeyVaultConfig)
            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Hashicorp Vault connection but no Vault configuration provided")));

        VaultEndpoint vaultEndpoint;
        try {
            vaultEndpoint = VaultEndpoint.from(new URI(keyVaultConfig.getUrl()));
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new ConfigException(new Throwable("Provided Hashicorp Vault url is incorrectly formatted", e));
        }

        ClientAuthentication clientAuthentication = new TokenAuthentication(authToken);

        return new HashicorpKeyVaultSpringService(
            keyVaultConfig,
            new VaultTemplate(
                vaultEndpoint,
                clientAuthentication
            )
        );
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
