package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.key.vault.KeyVaultException;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class AzureKeyVaultServiceTest {

    private AzureKeyVaultService keyVaultService;

    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    private String vaultUrl = "url";

    private AzureKeyVaultConfig keyVaultConfig;

    @Before
    public void setUp() {
        this.azureKeyVaultClientDelegate = mock(AzureKeyVaultClientDelegate.class);
        this.keyVaultConfig = mock(AzureKeyVaultConfig.class);
        when(keyVaultConfig.getUrl()).thenReturn(vaultUrl);

        this.keyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);
    }

    @Test
    public void getSecretGetsLatestVersionOfSecretIfNoVersionProvided() {
        String secretName = "name";

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);
        when(getSecretData.getSecretVersion()).thenReturn(null);

        SecretBundle secretBundle = mock(SecretBundle.class);
        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString())).thenReturn(secretBundle);
        when(secretBundle.value()).thenReturn("value");

        keyVaultService.getSecret(getSecretData);

        verify(azureKeyVaultClientDelegate).getSecret(vaultUrl, secretName);
    }

    @Test
    public void getSecretGetsSpecificVersionOfSecretIfVersionProvided() {
        String secretName = "name";
        String secretVersion = "version";

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);
        when(getSecretData.getSecretVersion()).thenReturn(secretVersion);

        SecretBundle secretBundle = mock(SecretBundle.class);
        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString(), anyString())).thenReturn(secretBundle);
        when(secretBundle.value()).thenReturn("value");

        keyVaultService.getSecret(getSecretData);

        verify(azureKeyVaultClientDelegate).getSecret(vaultUrl, secretName, secretVersion);
    }

    @Test
    public void getSecretThrowsExceptionIfKeyNotFoundInVault() {
        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString())).thenReturn(null);

        AzureKeyVaultService azureKeyVaultService =
                new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        String secretName = "secret";

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        Throwable throwable = catchThrowable(() -> azureKeyVaultService.getSecret(getSecretData));

        assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
        assertThat(throwable)
                .hasMessageContaining("Azure Key Vault secret " + secretName + " was not found in vault " + vaultUrl);
    }

    @Test
    public void getSecretThrowsExceptionIfWrongDataImplProvided() {
        GetSecretData wrongImpl = mock(GetSecretData.class);

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(wrongImpl));

        assertThat(ex).isInstanceOf(KeyVaultException.class);
        assertThat(ex.getMessage()).isEqualTo("Incorrect data type passed to AzureKeyVaultService.  Type was null");
    }

    @Test
    public void setSecret() {
        AzureSetSecretData setSecretData = mock(AzureSetSecretData.class);
        String secretName = "id";
        String secret = "secret";
        when(setSecretData.getSecretName()).thenReturn(secretName);
        when(setSecretData.getSecret()).thenReturn(secret);

        keyVaultService.setSecret(setSecretData);

        SetSecretRequest expected = new SetSecretRequest.Builder(vaultUrl, secretName, secret).build();

        ArgumentCaptor<SetSecretRequest> argument = ArgumentCaptor.forClass(SetSecretRequest.class);
        verify(azureKeyVaultClientDelegate).setSecret(argument.capture());

        assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void setSecretThrowsExceptionIfWrongDataImplProvided() {
        SetSecretData wrongImpl = mock(SetSecretData.class);

        Throwable ex = catchThrowable(() -> keyVaultService.setSecret(wrongImpl));

        assertThat(ex).isInstanceOf(KeyVaultException.class);
        assertThat(ex.getMessage()).isEqualTo("Incorrect data type passed to AzureKeyVaultService.  Type was null");
    }
}
