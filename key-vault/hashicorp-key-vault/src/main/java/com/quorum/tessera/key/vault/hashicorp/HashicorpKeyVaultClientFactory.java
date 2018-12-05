package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;

public class HashicorpKeyVaultClientFactory implements KeyVaultClientFactory {

    public Vault createUnauthenticatedClient(HashicorpKeyVaultConfig keyVaultConfig, VaultConfigFactory vaultConfigFactory, SslConfigFactory sslConfigFactory) {
        VaultConfig vaultConfig = createBaseVaultConfig(keyVaultConfig, vaultConfigFactory, sslConfigFactory);

        VaultCallback.execute(vaultConfig::build);

        return new Vault(vaultConfig);
    }

    public Vault createAuthenticatedClient(HashicorpKeyVaultConfig keyVaultConfig, VaultConfigFactory vaultConfigFactory, SslConfigFactory sslConfigFactory, String authToken) {
        VaultConfig vaultConfig = createBaseVaultConfig(keyVaultConfig, vaultConfigFactory, sslConfigFactory);

        vaultConfig.token(authToken);

        VaultCallback.execute(vaultConfig::build);

        return new Vault(vaultConfig);
    }

    private VaultConfig createBaseVaultConfig(HashicorpKeyVaultConfig keyVaultConfig, VaultConfigFactory vaultConfigFactory, SslConfigFactory sslConfigFactory) {
        VaultConfig vaultConfig = vaultConfigFactory.create()
                                                    .address(keyVaultConfig.getUrl());

        if (keyVaultConfig.getTlsServerCertificatePath() != null) {
            SslConfig sslConfig = sslConfigFactory.create();

            VaultCallback.execute(
                () -> sslConfig.pemFile(keyVaultConfig.getTlsServerCertificatePath().toFile())
            );

            if(keyVaultConfig.getTlsCertificatePath() != null && keyVaultConfig.getTlsKeyPath() != null) {
                VaultCallback.execute(
                    () -> sslConfig.clientPemFile(keyVaultConfig.getTlsCertificatePath().toFile())
                                   .clientKeyPemFile(keyVaultConfig.getTlsKeyPath().toFile())
                );
            }

            VaultCallback.execute(
                () -> sslConfig.build()
            );

            vaultConfig.sslConfig(sslConfig);
        }

        return vaultConfig;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
