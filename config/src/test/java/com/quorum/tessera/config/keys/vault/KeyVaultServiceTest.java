package com.quorum.tessera.config.keys.vault;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultServiceTest {
    @Test
    public void test() {
        String vaultUrl = "https://tesserakeyvault.vault.azure.net";

        KeyVaultService vaultService = new KeyVaultService(vaultUrl);
        String result = vaultService.getSecretValue("PrivateKey1");
        assertThat(result).isEqualTo("nDFwJNHSiT1gNzKBy9WJvMhmYRkW3TzFUmPsNzR6oFk=");
    }
}
