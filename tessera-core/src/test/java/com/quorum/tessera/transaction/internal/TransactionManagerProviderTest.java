package com.quorum.tessera.transaction.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.PrivacyHelper;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransactionManagerProviderTest {

  @Before
  @After
  public void clearHolder() {
    TransactionManagerHolder.INSTANCE.store(null);
    assertThat(TransactionManagerHolder.INSTANCE.getTransactionManager()).isNotPresent();
  }

  @Test
  public void provider() {

    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
        var mockedStaticEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
        var mockedStaticEnclave = mockStatic(Enclave.class);
        var mockedStaticEncryptedRawTransactionDAO = mockStatic(EncryptedRawTransactionDAO.class);
        var mockedStaticPayloadPublisher = mockStatic(PayloadPublisher.class);
        var mockedStaticBatchPayloadPublisher = mockStatic(BatchPayloadPublisher.class);
        var mockedStaticPrivacyHelper = mockStatic(PrivacyHelper.class);
        var mockedStaticResendManager = mockStatic(ResendManager.class);
        var mockedStaticPayloadDigest = mockStatic(PayloadDigest.class)) {

      ConfigFactory configFactory = mock(ConfigFactory.class);
      Config config = mock(Config.class);
      when(configFactory.getConfig()).thenReturn(config);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      PayloadPublisher payloadPublisher = mock(PayloadPublisher.class);

      mockedStaticPayloadPublisher.when(PayloadPublisher::create).thenReturn(payloadPublisher);

      BatchPayloadPublisher batchPayloadPublisher = mock(BatchPayloadPublisher.class);

      mockedStaticBatchPayloadPublisher
          .when(BatchPayloadPublisher::create)
          .thenReturn(batchPayloadPublisher);

      mockedStaticEncryptedRawTransactionDAO
          .when(EncryptedRawTransactionDAO::create)
          .thenReturn(mock(EncryptedRawTransactionDAO.class));

      mockedStaticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));

      mockedStaticEncryptedTransactionDAO
          .when(EncryptedTransactionDAO::create)
          .thenReturn(mock(EncryptedTransactionDAO.class));

      mockedStaticPrivacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));

      mockedStaticResendManager.when(ResendManager::create).thenReturn(mock(ResendManager.class));

      mockedStaticPayloadDigest.when(PayloadDigest::create).thenReturn(mock(PayloadDigest.class));

      TransactionManager transactionManager = TransactionManagerProvider.provider();
      assertThat(transactionManager).isNotNull();

      assertThat(TransactionManagerProvider.provider())
          .describedAs("Second invocation should return same instance")
          .isSameAs(transactionManager);

      mockedStaticEncryptedTransactionDAO.verify(EncryptedTransactionDAO::create);
      mockedStaticEncryptedTransactionDAO.verifyNoMoreInteractions();

      mockedStaticEnclave.verify(Enclave::create);
      mockedStaticEnclave.verifyNoMoreInteractions();

      mockedStaticEncryptedRawTransactionDAO.verify(EncryptedRawTransactionDAO::create);
      mockedStaticEncryptedRawTransactionDAO.verifyNoMoreInteractions();

      mockedStaticPayloadPublisher.verify(PayloadPublisher::create);
      mockedStaticPayloadPublisher.verifyNoMoreInteractions();

      mockedStaticBatchPayloadPublisher.verify(BatchPayloadPublisher::create);
      mockedStaticBatchPayloadPublisher.verifyNoMoreInteractions();

      mockedStaticPrivacyHelper.verify(PrivacyHelper::create);
      mockedStaticPrivacyHelper.verifyNoMoreInteractions();

      mockedStaticResendManager.verify(ResendManager::create);
      mockedStaticResendManager.verifyNoMoreInteractions();

      mockedStaticPayloadDigest.verify(PayloadDigest::create);
      mockedStaticPayloadDigest.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new TransactionManagerProvider()).isNotNull();
  }
}
