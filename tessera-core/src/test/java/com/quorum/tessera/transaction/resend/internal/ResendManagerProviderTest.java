package com.quorum.tessera.transaction.resend.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.junit.Test;

public class ResendManagerProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new ResendManagerProvider()).isNotNull();
  }

  @Test
  public void provider() {
    try (var mockedEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
        var mockedEnclave = mockStatic(Enclave.class);
        var mockedStaticPayloadDigest = mockStatic(PayloadDigest.class)) {

      Enclave enclave = mock(Enclave.class);

      mockedEnclave.when(Enclave::create).thenReturn(enclave);

      mockedEncryptedTransactionDAO
          .when(EncryptedTransactionDAO::create)
          .thenReturn(mock(EncryptedTransactionDAO.class));

      mockedStaticPayloadDigest.when(PayloadDigest::create).thenReturn(mock(PayloadDigest.class));

      ResendManager resendManager = ResendManagerProvider.provider();
      assertThat(resendManager).isNotNull();

      mockedEncryptedTransactionDAO.verify(EncryptedTransactionDAO::create);
      mockedEncryptedTransactionDAO.verifyNoMoreInteractions();

      mockedEnclave.verify(Enclave::create);
      mockedEnclave.verifyNoMoreInteractions();

      mockedStaticPayloadDigest.verify(PayloadDigest::create);
      mockedStaticPayloadDigest.verifyNoMoreInteractions();
    }
  }
}
