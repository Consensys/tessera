package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class EncodedPayloadManagerTest {

    @Test
    public void create() {

        try (
            var privacyHelper = mockStatic(PrivacyHelper.class);
            var staticEnclave = mockStatic(Enclave.class)) {

            privacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));

            staticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));

            final EncodedPayloadManager encodedPayloadManager = EncodedPayloadManager.create();

            assertThat(encodedPayloadManager)
                .isNotNull()
                .isInstanceOf(EncodedPayloadManagerImpl.class)
                .isSameAs(EncodedPayloadManager.getInstance().get());


        }
    }
}
