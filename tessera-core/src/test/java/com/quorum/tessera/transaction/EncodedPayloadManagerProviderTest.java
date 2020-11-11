package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EncodedPayloadManagerProviderTest {

    @Test
    public void defaultConstructOrForCoverage() {
        assertThat(new EncodedPayloadManagerProvider()).isNotNull();
    }

    @Test
    public void provider() {
        try(var mockedEnclaveFactory = mockStatic(EnclaveFactory.class);
            var mockedPrivacyHelper = mockStatic(PrivacyHelper.class)
        ) {

            mockedPrivacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));

            EnclaveFactory enclaveFactory = mock(EnclaveFactory.class);
            when(enclaveFactory.enclave()).thenReturn(Optional.of(mock(Enclave.class)));
            mockedEnclaveFactory.when(EnclaveFactory::create).thenReturn(enclaveFactory);


            EncodedPayloadManager encodedPayloadManager = EncodedPayloadManagerProvider.provider();
            assertThat(encodedPayloadManager).isNotNull();

            mockedPrivacyHelper.verify(PrivacyHelper::create);
            mockedPrivacyHelper.verifyNoMoreInteractions();

            mockedEnclaveFactory.verify(EnclaveFactory::create);
            mockedEnclaveFactory.verifyNoMoreInteractions();


        }
    }

}
