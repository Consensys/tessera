package com.quorum.tessera.key;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keys.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void keyDataContainsPubAndPrivKeysOnly() {
        String priv = "private";
        String pub = "public";
        KeyData keyData = new KeyData(null, priv, pub, null, null, null);

        KeyPair result = test.getKeyPair(keyData);
        KeyPair expected = new KeyPair(createKey(pub), createKey(priv));

        assertThat(result).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void ignoresVaultIdIfKeyDataContainsPubAndPrivKeys() {
        String priv = "private";
        String pub = "public";
        KeyData keyData = new KeyData(null, priv, pub, null, null, "vaultId");

        KeyPair result = test.getKeyPair(keyData);
        KeyPair expected = new KeyPair(createKey(pub), createKey(priv));

        assertThat(result).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void ifNoPrivateKeyThenVaultIdIsUsed() {
        String pub = "public";
        String vaultId = "vaultId";
        KeyData keyData = new KeyData(null, null, pub, null, null, vaultId);

        KeyPair result = test.getKeyPair(keyData);
        KeyPair expected = new KeyPair(createKey(pub), createKey(secret));

        assertThat(result).isEqualToComparingFieldByField(expected);
        verify(keyVaultService).getSecret(vaultId);
    }

}
