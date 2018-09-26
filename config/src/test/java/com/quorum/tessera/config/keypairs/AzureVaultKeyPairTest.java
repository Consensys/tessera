package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureVaultKeyPairTest {

    private AzureVaultKeyPair keyPair;

    @Before
    public void setUp() {
        keyPair = new AzureVaultKeyPair("pubId", "privId");
    }

    @Test
    public void getPrivateAndPublicKeysReturnsNull() {
        assertThat(keyPair.getPublicKey()).isNull();
        assertThat(keyPair.getPrivateKey()).isNull();
    }

    @Test
    public void getIdsReturnsConstructorSetValues() {
        assertThat(keyPair.getPublicKeyId()).isEqualTo("pubId");
        assertThat(keyPair.getPrivateKeyId()).isEqualTo("privId");
    }


    @Test
    public void getTypeReturnsAzure() {
        assertThat(keyPair.getType()).isEqualByComparingTo(ConfigKeyPairType.AZURE);
    }

    @Test
    public void marshallCreatesKeyDataObjectContainingIdsOnly() {
        KeyData actual = keyPair.marshal();
        KeyData expected = new KeyData(null, null, null, null, null, "privId", "pubId");

        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void getPasswordAlwaysReturnsEmptyString() {
        assertThat(keyPair.getPassword()).isEmpty();

        keyPair.withPassword("password");

        assertThat(keyPair.getPassword()).isEmpty();
    }
}
