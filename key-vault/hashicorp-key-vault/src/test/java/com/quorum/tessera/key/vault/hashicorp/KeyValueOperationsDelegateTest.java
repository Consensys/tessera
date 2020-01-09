package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyValueOperationsDelegateTest {

    private KeyValueOperationsDelegate delegate;

    private VaultVersionedKeyValueOperations keyValueOperations;

    @Before
    public void setUp() {
        this.keyValueOperations = mock(VaultVersionedKeyValueOperations.class);
        this.delegate = new KeyValueOperationsDelegate(keyValueOperations);
    }

    @Test
    public void get() {
        String secretName = "secretName";

        HashicorpGetSecretData getSecretData = mock(HashicorpGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);
        when(getSecretData.getSecretVersion()).thenReturn(0);

        Versioned versionedResponse = mock(Versioned.class);
        when(keyValueOperations.get(secretName, Versioned.Version.from(0))).thenReturn(versionedResponse);

        Versioned result = delegate.get(getSecretData);

        verify(keyValueOperations).get(secretName, Versioned.Version.unversioned());

        assertThat(result).isEqualTo(versionedResponse);
    }

    @Test
    public void set() {
        String secretName = "secretName";

        HashicorpSetSecretData setSecretData = mock(HashicorpSetSecretData.class);
        when(setSecretData.getSecretName()).thenReturn(secretName);
        Map nameValuePairs = mock(Map.class);
        when(setSecretData.getNameValuePairs()).thenReturn(nameValuePairs);

        Versioned.Metadata metadata = mock(Versioned.Metadata.class);
        when(keyValueOperations.put(secretName, nameValuePairs)).thenReturn(metadata);

        Versioned.Metadata result = delegate.set(setSecretData);

        verify(keyValueOperations).put(secretName, nameValuePairs);

        assertThat(result).isEqualTo(metadata);
    }
}
