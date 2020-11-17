package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionManagerProviderTest {



    @Test
    public void provider() {


        try(
            var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
            var mockedStaticEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
            var mockedStaticEnclave = mockStatic(Enclave.class);
            var mockedStaticEncryptedRawTransactionDAO = mockStatic(EncryptedRawTransactionDAO.class);
            var mockedStaticPayloadPublisherFactory = mockStatic(PayloadPublisherFactory.class);
            var mockedStaticBatchPayloadPublisherFactory = mockStatic(BatchPayloadPublisherFactory.class);
            var mockedStaticPrivacyHelper = mockStatic(PrivacyHelper.class);
            var mockedStaticResendManager = mockStatic(ResendManager.class);
            var mockedStaticMessageHashFactory = mockStatic(MessageHashFactory.class)
        ) {

            ConfigFactory configFactory = mock(ConfigFactory.class);
            Config config = mock(Config.class);
            when(configFactory.getConfig()).thenReturn(config);
            mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

            PayloadPublisherFactory payloadPublisherFactory = mock(PayloadPublisherFactory.class);
            PayloadPublisher payloadPublisher = mock(PayloadPublisher.class);
            when(payloadPublisherFactory.create(config)).thenReturn(payloadPublisher);

            mockedStaticPayloadPublisherFactory.when(() -> PayloadPublisherFactory.newFactory(config))
                .thenReturn(payloadPublisherFactory);

            BatchPayloadPublisherFactory batchPayloadPublisherFactory = mock(BatchPayloadPublisherFactory.class);
            when(batchPayloadPublisherFactory.create(payloadPublisher)).thenReturn(mock(BatchPayloadPublisher.class));
            mockedStaticBatchPayloadPublisherFactory.when(BatchPayloadPublisherFactory::newFactory)
                .thenReturn(batchPayloadPublisherFactory);

            mockedStaticEncryptedRawTransactionDAO.when(EncryptedRawTransactionDAO::create)
                .thenReturn(mock(EncryptedRawTransactionDAO.class));

            mockedStaticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));

            mockedStaticEncryptedTransactionDAO.when(EncryptedTransactionDAO::create)
                .thenReturn(mock(EncryptedTransactionDAO.class));

            mockedStaticPrivacyHelper.when(PrivacyHelper::create)
                .thenReturn(mock(PrivacyHelper.class));

            mockedStaticResendManager.when(ResendManager::create)
                .thenReturn(mock(ResendManager.class));


            mockedStaticMessageHashFactory.when(MessageHashFactory::create).thenReturn(mock(MessageHashFactory.class));

            TransactionManager transactionManager = TransactionManagerProvider.provider();
            assertThat(transactionManager).isNotNull();


            mockedStaticEncryptedTransactionDAO.verify(EncryptedTransactionDAO::create);
            mockedStaticEncryptedTransactionDAO.verifyNoMoreInteractions();

            mockedStaticEnclave.verify(Enclave::create);
            mockedStaticEnclave.verifyNoMoreInteractions();

            mockedStaticEncryptedRawTransactionDAO.verify(EncryptedRawTransactionDAO::create);
            mockedStaticEncryptedRawTransactionDAO.verifyNoMoreInteractions();

            mockedStaticPayloadPublisherFactory.verify(() -> PayloadPublisherFactory.newFactory(config));
            mockedStaticPayloadPublisherFactory.verifyNoMoreInteractions();

            mockedStaticBatchPayloadPublisherFactory.verify(BatchPayloadPublisherFactory::newFactory);
            mockedStaticBatchPayloadPublisherFactory.verifyNoMoreInteractions();

            mockedStaticPrivacyHelper.verify(PrivacyHelper::create);
            mockedStaticPrivacyHelper.verifyNoMoreInteractions();

            mockedStaticResendManager.verify(ResendManager::create);
            mockedStaticResendManager.verifyNoMoreInteractions();

            mockedStaticMessageHashFactory.verify(MessageHashFactory::create);
            mockedStaticMessageHashFactory.verifyNoMoreInteractions();

        }

    }

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new TransactionManagerProvider()).isNotNull();
    }

}
