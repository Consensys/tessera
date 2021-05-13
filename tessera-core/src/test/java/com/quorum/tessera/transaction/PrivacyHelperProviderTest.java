package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.PayloadEncoder;
import org.junit.Test;

public class PrivacyHelperProviderTest {

  @Test
  public void defaultContstructorForCoverage() {
    assertThat(new PrivacyHelperProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var mockedRuntimeContext = mockStatic(RuntimeContext.class);
        var mockedEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
        var mockedPayloadEncoder = mockStatic(PayloadEncoder.class)) {
      RuntimeContext runtimeContext = mock(RuntimeContext.class);
      when(runtimeContext.isEnhancedPrivacy()).thenReturn(true);
      mockedRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      mockedEncryptedTransactionDAO
          .when(EncryptedTransactionDAO::create)
          .thenReturn(mock(EncryptedTransactionDAO.class));

      mockedPayloadEncoder.when(PayloadEncoder::create).thenReturn(mock(PayloadEncoder.class));

      PrivacyHelper privacyHelper = PrivacyHelperProvider.provider();

      assertThat(privacyHelper).isNotNull();
    }
  }
}
