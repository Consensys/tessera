package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class EncodedPayloadManagerProviderTest {

    @Test
    public void defaultConstructOrForCoverage() {
        assertThat(new EncodedPayloadManagerProvider()).isNotNull();
    }

    @Test
    public void provider() {
        try(var mockedEnclaveFactory = mockStatic(Enclave.class);
            var mockedPrivacyHelper = mockStatic(PrivacyHelper.class)
        ) {

            mockedPrivacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));
            mockedEnclaveFactory.when(Enclave::create).thenReturn(mock(Enclave.class));

            EncodedPayloadManager encodedPayloadManager = EncodedPayloadManagerProvider.provider();
            assertThat(encodedPayloadManager).isNotNull();
            assertThat(encodedPayloadManager)
                .describedAs("Subsequent invocations shoudl return teh same instance")
                .isSameAs(EncodedPayloadManagerProvider.provider());

            mockedPrivacyHelper.verify(PrivacyHelper::create);
            mockedPrivacyHelper.verifyNoMoreInteractions();

            mockedEnclaveFactory.verify(Enclave::create);
            mockedEnclaveFactory.verifyNoMoreInteractions();


        }
    }

}
