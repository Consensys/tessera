package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.response.AuthResponse;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;

public class HashicorpKeyVaultClientFactory implements KeyVaultClientFactory {

    private VaultConfig vaultConfig;

    private SslConfig sslConfig;

    private Vault vault;

    public HashicorpKeyVaultClientFactory init(HashicorpKeyVaultConfig keyVaultConfig, VaultConfigFactory vaultConfigFactory, SslConfigFactory sslConfigFactory) {

        this.vaultConfig = vaultConfigFactory.create()
                                             .address(keyVaultConfig.getUrl());

        if(keyVaultConfig.getTlsCertificatePath() != null) {
            this.sslConfig = sslConfigFactory.create();

            VaultCallback.execute(
                () -> sslConfig.pemFile(keyVaultConfig.getTlsCertificatePath().toFile())
                               .build()
            );

            vaultConfig.sslConfig(sslConfig);
        }

        VaultCallback.execute(vaultConfig::build);

        this.vault = new Vault(vaultConfig);

        return this;
    }

    public HashicorpKeyVaultClientFactory login(String roleId, String secretId, String authToken) {
        String token;

        if(roleId != null && secretId != null) {
            AuthResponse loginResponse = VaultCallback.execute(() -> vault.auth().loginByAppRole("approle", roleId, secretId));
            token = loginResponse.getAuthClientToken();
        } else {
            token = authToken;
        }

        this.vaultConfig.token(token);

        return this;
    }

    public Vault create() {
        return this.vault;
    }

}
