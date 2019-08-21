package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.vault.support.Versioned;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashicorpKeyVaultServiceTest {

    private HashicorpKeyVaultService keyVaultService;

    private KeyValueOperationsDelegateFactory delegateFactory;

    private KeyValueOperationsDelegate delegate;

    @Before
    public void setUp() {
        this.delegateFactory = mock(KeyValueOperationsDelegateFactory.class);
        this.delegate = mock(KeyValueOperationsDelegate.class);
        when(delegateFactory.create(anyString())).thenReturn(delegate);

        this.keyVaultService = new HashicorpKeyVaultService(delegateFactory);
    }

    @Test
    public void getSecret() {
        HashicorpGetSecretData getSecretData = mock(HashicorpGetSecretData.class);

        when(getSecretData.getSecretEngineName()).thenReturn("secretEngine");
        when(getSecretData.getSecretName()).thenReturn("secretName");
        when(getSecretData.getValueId()).thenReturn("keyId");

        Versioned versionedResponse = mock(Versioned.class);

        when(delegate.get(any(HashicorpGetSecretData.class))).thenReturn(versionedResponse);

        when(versionedResponse.hasData()).thenReturn(true);

        Map responseData = mock(Map.class);
        when(versionedResponse.getData()).thenReturn(responseData);
        when(responseData.containsKey("keyId")).thenReturn(true);
        String keyValue = "keyvalue";
        when(responseData.get("keyId")).thenReturn(keyValue);

        String result = keyVaultService.getSecret(getSecretData);

        assertThat(result).isEqualTo(keyValue);
    }

    @Test
    public void getSecretThrowsExceptionIfNullRetrievedFromVault() {
        HashicorpGetSecretData getSecretData = new HashicorpGetSecretData("engine", "secretName", "id", 0);

        when(delegate.get(getSecretData)).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("No data found at engine/secretName");
    }

    @Test
    public void getSecretThrowsExceptionIfNoDataRetrievedFromVault() {
        HashicorpGetSecretData getSecretData = new HashicorpGetSecretData("engine", "secretName", "id", 0);

        Versioned versionedResponse = mock(Versioned.class);
        when(versionedResponse.hasData()).thenReturn(false);

        when(delegate.get(getSecretData)).thenReturn(versionedResponse);

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("No data found at engine/secretName");
    }

    @Test
    public void getSecretThrowsExceptionIfValueNotFoundForGivenId() {
        HashicorpGetSecretData getSecretData = new HashicorpGetSecretData("engine", "secretName", "id", 0);

        Versioned versionedResponse = mock(Versioned.class);
        when(versionedResponse.hasData()).thenReturn(true);

        Map responseData = mock(Map.class);
        when(versionedResponse.getData()).thenReturn(responseData);
        when(responseData.containsKey("id")).thenReturn(false);

        when(delegate.get(getSecretData)).thenReturn(versionedResponse);

        Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessage("No value with id id found at engine/secretName");
    }

    @Test
    public void setSecretReturnsMetadataObject() {
        HashicorpSetSecretData setSecretData = new HashicorpSetSecretData("engine", "name", Collections.emptyMap());

        Versioned.Metadata metadata = mock(Versioned.Metadata.class);
        when(delegate.set(setSecretData)).thenReturn(metadata);

        Object result = keyVaultService.setSecret(setSecretData);

        assertThat(result).isInstanceOf(Versioned.Metadata.class);
        assertThat(result).isEqualTo(metadata);
    }

    @Test
    public void setSecretIfNullPointerExceptionThenHashicorpExceptionThrown() {
        HashicorpSetSecretData setSecretData = mock(HashicorpSetSecretData.class);

        when(delegate.set(any(HashicorpSetSecretData.class))).thenThrow(new NullPointerException());

        Throwable ex = catchThrowable(() -> keyVaultService.setSecret(setSecretData));

        assertThat(ex).isExactlyInstanceOf(HashicorpVaultException.class);
        assertThat(ex.getMessage()).isEqualTo("Unable to save generated secret to vault.  Ensure that the secret engine being used is a v2 kv secret engine");
    }
}
