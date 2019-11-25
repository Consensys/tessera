package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.keypairs.AWSKeyPair;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.vault.data.AWSSetSecretData;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.jnacl.Jnacl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AWSSecretManagerKeyGeneratorTest {

    private final String pubStr = "public";
    private final String privStr = "private";
    private final PublicKey pub = PublicKey.from(pubStr.getBytes());
    private final PrivateKey priv = PrivateKey.from(privStr.getBytes());

    private Jnacl jnacl;
    private KeyVaultService keyVaultService;
    private AWSSecretManagerKeyGenerator awsSecretManagerKeyGenerator;

    @Before
    public void setUp() {
        this.jnacl = mock(Jnacl.class);
        this.keyVaultService = mock(KeyVaultService.class);

        final KeyPair keyPair = new KeyPair(pub, priv);

        when(jnacl.generateNewKeys()).thenReturn(keyPair);

        awsSecretManagerKeyGenerator = new AWSSecretManagerKeyGenerator(jnacl, keyVaultService);
    }

    @Test
    public void keysSavedInVaultWithProvidedVaultIdAndCorrectSuffix() {
        final String vaultId = "vaultId";
        final String pubVaultId = vaultId + "Pub";
        final String privVaultId = vaultId + "Key";

        final AWSKeyPair result = awsSecretManagerKeyGenerator.generate(vaultId, null, null);

        final ArgumentCaptor<AWSSetSecretData> captor = ArgumentCaptor.forClass(AWSSetSecretData.class);

        verify(keyVaultService, times(2)).setSecret(captor.capture());

        List<AWSSetSecretData> capturedArgs = captor.getAllValues();
        assertThat(capturedArgs).hasSize(2);

        AWSSetSecretData expectedDataPub = new AWSSetSecretData(pubVaultId, pub.encodeToBase64());
        AWSSetSecretData expectedDataPriv = new AWSSetSecretData(privVaultId, priv.encodeToBase64());

        assertThat(capturedArgs)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expectedDataPub, expectedDataPriv);

        verifyNoMoreInteractions(keyVaultService);

        final AzureVaultKeyPair expected = new AzureVaultKeyPair(pubVaultId, privVaultId, null, null);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void exceptionThrownIfDisallowedCharactersUsedInVaultId() {
        final String invalidId = "/tmp/abc@+!";

        final Throwable throwable = catchThrowable(() -> awsSecretManagerKeyGenerator.generate(invalidId, null, null));

        assertThat(throwable).isInstanceOf(UnsupportedCharsetException.class);
        assertThat(throwable)
                .hasMessageContaining(
                        "Generated key ID for AWS Secret Manager can contain only 0-9, a-z, A-Z and /_+=.@- characters");
    }
}
