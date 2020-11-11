package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EncodedPayloadManagerTest {

    @Test
    public void create() {

        try (
            var privacyHelper = mockStatic(PrivacyHelper.class);
            var enclaveFactory = mockStatic(EnclaveFactory.class)) {

            privacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));

            EnclaveFactory mocked = mock(EnclaveFactory.class);
            when(mocked.enclave()).thenReturn(Optional.of(mock(Enclave.class)));
            enclaveFactory.when(EnclaveFactory::create).thenReturn(mocked);

            final EncodedPayloadManager encodedPayloadManager = EncodedPayloadManager.create();

            assertThat(encodedPayloadManager)
                .isNotNull()
                .isInstanceOf(EncodedPayloadManagerImpl.class)
                .isSameAs(EncodedPayloadManager.getInstance().get());


        }
    }
}
