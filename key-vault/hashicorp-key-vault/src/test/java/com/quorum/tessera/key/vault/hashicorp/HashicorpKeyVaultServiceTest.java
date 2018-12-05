package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.key.vault.KeyVaultException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashicorpKeyVaultServiceTest {

    private HashicorpKeyVaultService keyVaultService;

    private Vault vault;

    @Before
    public void setUp() {
        this.vault = mock(Vault.class);
        this.keyVaultService = new HashicorpKeyVaultService(vault);
    }

    @Test
    public void getSecretThrowsExceptionIfProvidedDataIsNotCorrectType() {
        GetSecretData getSecretData = mock(GetSecretData.class);
        when(getSecretData.getType()).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(KeyVaultException.class);
        assertThat(ex).hasMessage("Incorrect data type passed to HashicorpKeyVaultService.  Type was null");
    }

    @Test
    public void getSecretThrowsExceptionIfErrorRetrievingSecretFromVault() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);
        when(logical.read(anyString())).thenThrow(new VaultException("vault exception msg"));

        GetSecretData getSecretData = new HashicorpGetSecretData("secret/path", "secretName");

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("Error getting secret secretName from path secret/path - vault exception msg");
    }

    @Test
    public void getSecretThrowsExceptionIfResponseHasNoData() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);

        LogicalResponse response = mock(LogicalResponse.class);
        when(logical.read(anyString())).thenReturn(response);

        when(response.getData()).thenReturn(null);

        GetSecretData getSecretData = new HashicorpGetSecretData("secret/path", "secretName");

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("No secret secretName found at path secret/path");
    }

    @Test
    public void getSecretThrowsExceptionIfValueNotFoundForGivenSecretName() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);

        LogicalResponse response = mock(LogicalResponse.class);
        when(logical.read(anyString())).thenReturn(response);

        Map<String, String> data = Collections.singletonMap("diffName", "value");
        when(response.getData()).thenReturn(data);

        GetSecretData getSecretData = new HashicorpGetSecretData("secret/path", "secretName");

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("No secret secretName found at path secret/path");
    }

    @Test
    public void getSecretReturnsValueForGivenSecretNameAtGivenPath() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);

        LogicalResponse response = mock(LogicalResponse.class);
        when(logical.read(anyString())).thenReturn(response);

        String expected = "value";

        Map<String, String> data = Collections.singletonMap("secretName", expected);
        when(response.getData()).thenReturn(data);

        GetSecretData getSecretData = new HashicorpGetSecretData("secret/path", "secretName");

        String result = keyVaultService.getSecret(getSecretData);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getSecretReturnsCorrectValueIfMultipleFoundAtGivenPath() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);

        LogicalResponse response = mock(LogicalResponse.class);
        when(logical.read(anyString())).thenReturn(response);

        String expected = "value";

        Map<String, String> data = new HashMap<>();
        data.put("someOtherSecret", "someOtherValue");
        data.put("secretName", expected);
        when(response.getData()).thenReturn(data);

        GetSecretData getSecretData = new HashicorpGetSecretData("secret/path", "secretName");

        String result = keyVaultService.getSecret(getSecretData);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void setSecretThrowsExceptionIfProvidedDataIsNotCorrectType() {
        SetSecretData setSecretData = mock(SetSecretData.class);
        when(setSecretData.getType()).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultService.setSecret(setSecretData));

        assertThat(ex).isExactlyInstanceOf(KeyVaultException.class);
        assertThat(ex).hasMessage("Incorrect data type passed to HashicorpKeyVaultService.  Type was null");
    }

    @Test
    public void setSecretThrowsExceptionIfErrorWritingToVault() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);
        when(logical.write(anyString(), anyMap())).thenThrow(new VaultException("vault exception msg"));

        SetSecretData setSecretData = new HashicorpSetSecretData("secret/path", Collections.emptyMap());

        Throwable ex = catchThrowable(() -> keyVaultService.setSecret(setSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("Error writing secret to path secret/path - vault exception msg");
    }

    @Test
    public void setSecretReturnsLogicalResponse() throws Exception {
        Logical logical = mock(Logical.class);
        when(vault.logical()).thenReturn(logical);

        LogicalResponse response = mock(LogicalResponse.class);
        when(logical.write(anyString(), anyMap())).thenReturn(response);

        SetSecretData setSecretData = new HashicorpSetSecretData("secret/path", Collections.emptyMap());

        Object result = keyVaultService.setSecret(setSecretData);

        assertThat(result).isInstanceOf(LogicalResponse.class);
        assertThat(result).isEqualTo(response);
    }

}
