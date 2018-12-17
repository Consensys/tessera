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
    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    @Before
    public void setUp() {
        this.azureKeyVaultClientDelegate = mock(AzureKeyVaultClientDelegate.class);
    }

    @Test
    public void getSecretExceptionThrownIfKeyNotFoundInVault() {
        String secretName = "secret";
        String vaultUrl = "vaultUrl";

        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig(vaultUrl);

        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString())).thenReturn(null);

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        Throwable throwable = catchThrowable(() -> azureKeyVaultService.getSecret(getSecretData));

        assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
        assertThat(throwable).hasMessageContaining("Azure Key Vault secret " + secretName + " was not found in vault " + vaultUrl);
    }

    @Test
    public void getSecretUsingUrlInConfig() {
        String url = "url";
        String secretId = "id";

        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig(url);

        when(azureKeyVaultClientDelegate.getSecret(url, secretId)).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretId);

        azureKeyVaultService.getSecret(getSecretData);

        verify(azureKeyVaultClientDelegate).getSecret(url, secretId);
    }

    @Test
    public void vaultUrlIsNotSetIfKeyVaultConfigNotDefined() {
        when(azureKeyVaultClientDelegate.getSecret(any(), any())).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(null, azureKeyVaultClientDelegate);

        AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn("secret");


        azureKeyVaultService.getSecret(getSecretData);

        verify(azureKeyVaultClientDelegate).getSecret(null, "secret");
    }

    @Test
    public void getSecretThrowsExceptionIfWrongDataImplProvided() {
        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(null, azureKeyVaultClientDelegate);

        GetSecretData wrongImpl = mock(GetSecretData.class);

        Throwable ex = catchThrowable(() -> azureKeyVaultService.getSecret(wrongImpl));

        assertThat(ex).isInstanceOf(KeyVaultException.class);
        assertThat(ex.getMessage()).isEqualTo("Incorrect data type passed to AzureKeyVaultService.  Type was null");
    }

    @Test
    public void setSecretRequestIsUsedToRetrieveSecretFromVault() {
        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("url");

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        String secretName = "id";
        String secret = "secret";

        AzureSetSecretData setSecretData = mock(AzureSetSecretData.class);
        when(setSecretData.getSecretName()).thenReturn(secretName);
        when(setSecretData.getSecret()).thenReturn(secret);

        azureKeyVaultService.setSecret(setSecretData);

        SetSecretRequest expected = new SetSecretRequest.Builder(keyVaultConfig.getUrl(), secretName, secret).build();

        ArgumentCaptor<SetSecretRequest> argument = ArgumentCaptor.forClass(SetSecretRequest.class);
        verify(azureKeyVaultClientDelegate).setSecret(argument.capture());

        assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void setSecretThrowsExceptionIfWrongDataImplProvided() {
        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(null, azureKeyVaultClientDelegate);

        SetSecretData wrongImpl = mock(SetSecretData.class);

        Throwable ex = catchThrowable(() -> azureKeyVaultService.setSecret(wrongImpl));

        assertThat(ex).isInstanceOf(KeyVaultException.class);
        assertThat(ex.getMessage()).isEqualTo("Incorrect data type passed to AzureKeyVaultService.  Type was null");
    }
}
