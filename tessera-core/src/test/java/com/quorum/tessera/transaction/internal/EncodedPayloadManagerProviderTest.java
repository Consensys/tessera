package com.quorum.tessera.transaction.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.PrivacyHelper;
import org.junit.Test;

public class EncodedPayloadManagerProviderTest {

  @Test
  public void defaultConstructOrForCoverage() {
    assertThat(new EncodedPayloadManagerProvider()).isNotNull();
  }

  @Test
  public void provider() {
    try (var mockedEnclaveFactory = mockStatic(Enclave.class);
        var mockedPrivacyHelper = mockStatic(PrivacyHelper.class);
        var mockedPayloadDigest = mockStatic(PayloadDigest.class)) {

      mockedPayloadDigest.when(PayloadDigest::create).thenReturn(mock(PayloadDigest.class));

      mockedPrivacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));
      mockedEnclaveFactory.when(Enclave::create).thenReturn(mock(Enclave.class));

      EncodedPayloadManager encodedPayloadManager = EncodedPayloadManagerProvider.provider();
      assertThat(encodedPayloadManager).isNotNull();
      assertThat(encodedPayloadManager)
          .describedAs("Subsequent invocations should return the same instance")
          .isSameAs(EncodedPayloadManagerProvider.provider());

      mockedPrivacyHelper.verify(PrivacyHelper::create);
      mockedPrivacyHelper.verifyNoMoreInteractions();

      mockedEnclaveFactory.verify(Enclave::create);
      mockedEnclaveFactory.verifyNoMoreInteractions();

      mockedPayloadDigest.verify(PayloadDigest::create);
      mockedPayloadDigest.verifyNoMoreInteractions();
    }
  }
}
