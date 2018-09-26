package com.quorum.tessera.key.generation;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class AzureVaultKeyGeneratorTest {

    private final String pubStr = "public";
    private final String privStr = "private";
    private final Key pub = new Key(pubStr.getBytes());
    private final Key priv = new Key(privStr.getBytes());

    private NaclFacade naclFacade;
    private KeyVaultService keyVaultService;
    private AzureVaultKeyGenerator azureVaultKeyGenerator;

    @Before
    public void setUp() {
        this.naclFacade = mock(NaclFacade.class);
        this.keyVaultService = mock(KeyVaultService.class);

        final KeyPair keyPair = new KeyPair(pub, priv);

        when(naclFacade.generateNewKeys()).thenReturn(keyPair);
        when(keyVaultService.setSecret("id", "secret")).thenReturn(new SecretBundle());

        azureVaultKeyGenerator = new AzureVaultKeyGenerator(naclFacade, keyVaultService);
    }

    @Test
    public void keysSavedInVaultWithProvidedVaultId() {
        final String vaultId = "vaultId";
        final String pubVaultId = vaultId + "Pub";
        final String privVaultId = vaultId + "Key";

        final AzureVaultKeyPair result = azureVaultKeyGenerator.generate(vaultId, null);

        verify(keyVaultService, times(2)).setSecret(any(String.class), any(String.class));
        verify(keyVaultService, times(1)).setSecret(vaultId + "Pub", pub.toString());
        verify(keyVaultService, times(1)).setSecret(vaultId + "Key", priv.toString());
        verifyNoMoreInteractions(keyVaultService);

        final AzureVaultKeyPair expected = new AzureVaultKeyPair(pubVaultId, privVaultId);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void publicKeyIsSavedToVaultAndIdHasPubSuffix() {
        final String vaultId = "vaultId";

        azureVaultKeyGenerator.generate(vaultId, null);

        verify(keyVaultService, times(1)).setSecret(vaultId + "Pub", pub.toString());
    }

    @Test
    public void privateKeyIsSavedToVaultAndIdHasKeySuffix() {
        final String vaultId = "vaultId";

        azureVaultKeyGenerator.generate(vaultId, null);

        verify(keyVaultService, times(1)).setSecret(vaultId + "Key", priv.toString());
    }

    @Test
    public void vaultIdIsFinalComponentOfFilePath() {
        final String vaultId = "vaultId";
        final String path = "/some/path/" + vaultId;

        azureVaultKeyGenerator.generate(path, null);

        verify(keyVaultService, times(1)).setSecret(vaultId + "Pub", pub.toString());
        verify(keyVaultService, times(1)).setSecret(vaultId + "Key", priv.toString());
    }

    @Test
    public void ifNoVaultIdProvidedThenSuffixOnlyIsUsed() {
        azureVaultKeyGenerator.generate(null, null);

        verify(keyVaultService, times(1)).setSecret("Pub", pub.toString());
        verify(keyVaultService, times(1)).setSecret("Key", priv.toString());
    }

    @Test
    public void allowedCharactersUsedInVaultIdDoesNotThrowException() {
        final String allowedId = "abcdefghijklmnopqrstuvwxyz-ABCDEFDGHIJKLMNOPQRSTUVWXYZ-0123456789";

        azureVaultKeyGenerator.generate(allowedId, null);

        verify(keyVaultService, times(2)).setSecret(any(String.class), any(String.class));
    }

    @Test
    public void exceptionThrownIfDisallowedCharactersUsedInVaultId() {
        final String invalidId = "!@£$%^&*()";

        final Throwable throwable = catchThrowable(
            () -> azureVaultKeyGenerator.generate(invalidId, null)
        );

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessageContaining(
            "Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters"
        );
    }

    @Test
    public void encryptionIsNotUsedWhenSavingToVault() {
        final ArgonOptions argonOptions = mock(ArgonOptions.class);

        azureVaultKeyGenerator.generate("vaultId", argonOptions);

        verifyNoMoreInteractions(argonOptions);
    }
}
