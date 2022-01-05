package com.quorum.tessera.transaction.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.transaction.PrivacyHelper;
import org.junit.Test;

public class PrivacyHelperProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new PrivacyHelperProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var mockedRuntimeContext = mockStatic(RuntimeContext.class);
        var mockedEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class)) {
      RuntimeContext runtimeContext = mock(RuntimeContext.class);
      when(runtimeContext.isEnhancedPrivacy()).thenReturn(true);
      mockedRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      mockedEncryptedTransactionDAO
          .when(EncryptedTransactionDAO::create)
          .thenReturn(mock(EncryptedTransactionDAO.class));

      PrivacyHelper privacyHelper = PrivacyHelperProvider.provider();

      assertThat(privacyHelper).isNotNull();
    }
  }
}
