package com.quorum.tessera.key;

import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class KeyPairFactoryTest {
    KeyPairFactory test;
    KeyVaultService keyVaultService;
    String secret = "secret";

    @Before
    public void setUp() {
        keyVaultService = mock(KeyVaultService.class);
        when(keyVaultService.getSecret(anyString())).thenReturn(secret);

        this.test = new KeyPairFactory(keyVaultService);
    }

    public Key createKey(String value) {
        return new Key(Base64.getDecoder().decode(value));
    }

    @Test
    public void keyPairCreatedUsingDirectKeyPair() {
        String pub = "pub";
        String priv = "priv";

        DirectKeyPair directKeyPair = new DirectKeyPair(pub, priv);

        KeyPair result = test.getKeyPair(directKeyPair);

        assertThat(result.getPublicKey()).isEqualToComparingFieldByField(createKey(pub));
        assertThat(result.getPrivateKey()).isEqualToComparingFieldByField(createKey(priv));
    }

    @Test
    public void keyPairCreatedUsingFilesystemKeyPair() {
        String pub = "pub";
        String priv = "priv";

        FilesystemKeyPair filesystemKeyPair = mock(FilesystemKeyPair.class);
        when(filesystemKeyPair.getPublicKey()).thenReturn(pub);
        when(filesystemKeyPair.getPrivateKey()).thenReturn(priv);

        KeyPair result = test.getKeyPair(filesystemKeyPair);

        assertThat(result.getPublicKey()).isEqualToComparingFieldByField(createKey(pub));
        assertThat(result.getPrivateKey()).isEqualToComparingFieldByField(createKey(priv));
    }

    @Test
    public void keyPairCreatedUsingInlineKeyPair() {
        String pub = "pub";
        String priv = "priv";

        InlineKeypair inlineKeypair = mock(InlineKeypair.class);
        when(inlineKeypair.getPublicKey()).thenReturn(pub);
        when(inlineKeypair.getPrivateKey()).thenReturn(priv);

        KeyPair result = test.getKeyPair(inlineKeypair);

        assertThat(result.getPublicKey()).isEqualToComparingFieldByField(createKey(pub));
        assertThat(result.getPrivateKey()).isEqualToComparingFieldByField(createKey(priv));
    }

    @Test
    public void azureVaultKeyPairFetchesKeysFromVault() {
        String publicId = "publicId";
        String privateId = "privateId";

        AzureVaultKeyPair azureVaultKeyPair = new AzureVaultKeyPair(publicId, privateId);

        test.getKeyPair(azureVaultKeyPair);

        verify(keyVaultService, times(1)).getSecret(publicId);
        verify(keyVaultService, times(1)).getSecret(privateId);
    }

}
