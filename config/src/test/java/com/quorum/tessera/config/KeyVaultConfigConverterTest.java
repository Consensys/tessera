package com.quorum.tessera.config;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultConfigConverterTest {

    @Test
    public void convertAzure() {
        AzureKeyVaultConfig azureKeyVaultConfig = new AzureKeyVaultConfig();
        azureKeyVaultConfig.setUrl("SomeUrl");

        DefaultKeyVaultConfig result = KeyVaultConfigConverter.convert(azureKeyVaultConfig);

        assertThat(result.getKeyVaultType()).isEqualTo(KeyVaultType.AZURE);
        assertThat(result.getProperties()).containsKeys("url");
        assertThat(result.getProperties().get("url")).isEqualTo("SomeUrl");
    }

    @Test
    public void convertHashicorp() {
        HashicorpKeyVaultConfig hashicorpKeyVaultConfig = new HashicorpKeyVaultConfig();
        hashicorpKeyVaultConfig.setUrl("SomeUrl");
        Path path = Paths.get("SomePath");
        hashicorpKeyVaultConfig.setTlsKeyStorePath(path);
        hashicorpKeyVaultConfig.setTlsTrustStorePath(path);

        DefaultKeyVaultConfig result = KeyVaultConfigConverter.convert(hashicorpKeyVaultConfig);

        assertThat(result.getKeyVaultType()).isEqualTo(KeyVaultType.HASHICORP);
        assertThat(result.getProperties()).containsKeys("url", "tlsKeyStorePath", "tlsTrustStorePath");
        assertThat(result.getProperties().get("url")).isEqualTo(hashicorpKeyVaultConfig.getUrl());
        assertThat(result.getProperties().get("tlsTrustStorePath")).isEqualTo(path.toString());
        assertThat(result.getProperties().get("tlsKeyStorePath")).isEqualTo(path.toString());
        assertThat(result.getProperties().get("approlePath")).isEqualTo(hashicorpKeyVaultConfig.getApprolePath());
    }
}
