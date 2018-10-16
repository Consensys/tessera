package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyPairConverterTest {

    private KeyPairConverter converter;
    private KeyVaultService keyVaultService;

    @Before
    public void setUp() {
        this.keyVaultService = mock(KeyVaultService.class);
        this.converter = new KeyPairConverter(keyVaultService);
    }

    private byte[] decodeBase64(String input) {
        return Base64.getDecoder().decode(input);
    }

    @Test
    public void convertSingleDirectKeyPair() {
        final DirectKeyPair keyPair = new DirectKeyPair("public", "private");
        Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

        assertThat(result).hasSize(1);

        KeyPair expected = new KeyPair(PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
        KeyPair resultKeyPair = result.iterator().next();

        assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void convertSingleFilesystemKeyPair() {
        final FilesystemKeyPair keyPair = mock(FilesystemKeyPair.class);
        when(keyPair.getPublicKey()).thenReturn("public");
        when(keyPair.getPrivateKey()).thenReturn("private");

        Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

        assertThat(result).hasSize(1);

        KeyPair expected = new KeyPair(PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
        KeyPair resultKeyPair = result.iterator().next();

        assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void convertSingleInlineKeyPair() {
        final InlineKeypair keyPair = mock(InlineKeypair.class);
        when(keyPair.getPublicKey()).thenReturn("public");
        when(keyPair.getPrivateKey()).thenReturn("private");

        Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

        assertThat(result).hasSize(1);

        KeyPair expected = new KeyPair(PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
        KeyPair resultKeyPair = result.iterator().next();

        assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void convertSingleAzureVaultKeyPair() {
        final AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);
        when(keyPair.getPublicKeyId()).thenReturn("pub");
        when(keyPair.getPrivateKeyId()).thenReturn("priv");

        when(keyVaultService.getSecret("pub")).thenReturn("publicSecret");
        when(keyVaultService.getSecret("priv")).thenReturn("privSecret");

        Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

        assertThat(result).hasSize(1);

        KeyPair resultKeyPair = result.iterator().next();
        KeyPair expected = new KeyPair(PublicKey.from(decodeBase64("publicSecret")), PrivateKey.from(decodeBase64("privSecret")));

        assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
    }


    @Test
    public void convertMultipleKeyPairs() {
        final String pubA = "publicA";
        final String privA = "privateA";
        final String pubB = "publicB";
        final String privB = "privateB";

        final DirectKeyPair keyPairA = new DirectKeyPair(pubA, privA);
        final DirectKeyPair keyPairB = new DirectKeyPair(pubB, privB);

        final Collection<KeyPair> result = converter.convert(Arrays.asList(keyPairA, keyPairB));

        assertThat(result).hasSize(2);

        final KeyPair expectedA = new KeyPair(PublicKey.from(decodeBase64(pubA)), PrivateKey.from(decodeBase64(privA)));
        final KeyPair expectedB = new KeyPair(PublicKey.from(decodeBase64(pubB)), PrivateKey.from(decodeBase64(privB)));

        final Iterator<KeyPair> it = result.iterator();

        final KeyPair resultA = it.next();
        assertThat(resultA).isEqualToComparingFieldByField(expectedA);

        final KeyPair resultB = it.next();
        assertThat(resultB).isEqualToComparingFieldByField(expectedB);
    }
}
