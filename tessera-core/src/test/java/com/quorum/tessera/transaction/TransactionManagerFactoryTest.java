package com.quorum.tessera.transaction;

import com.quorum.tessera.config.*;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionManagerFactoryTest {

    @Test
    public void create() {
        assertThat(TransactionManagerFactory.create()).isNotNull();
    }

    @Test
    public void createTransactionManager() {

        try(
            var mockedStaticPayloadPublisherFactory = mockStatic(PayloadPublisherFactory.class);
            var mockedStaticBatchPayloadPublisherFactory = mockStatic(BatchPayloadPublisherFactory.class);
            var mockedStaticEnclave = mockStatic(Enclave.class);
            var mockedStaticEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
            var mockedStaticEncryptedRawTransactionDAO = mockStatic(EncryptedRawTransactionDAO.class);
            var mockedStaticPrivacyHelper = mockStatic(PrivacyHelper.class)
            ) {

            mockedStaticPrivacyHelper.when(PrivacyHelper::create).thenReturn(mock(PrivacyHelper.class));

            EncryptedRawTransactionDAO encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);

            mockedStaticEncryptedRawTransactionDAO.when(EncryptedRawTransactionDAO::create)
                .thenReturn(encryptedRawTransactionDAO);

            EncryptedTransactionDAO encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);

            mockedStaticEncryptedTransactionDAO.when(EncryptedTransactionDAO::create).thenReturn(encryptedTransactionDAO);

            mockedStaticEnclave.when(Enclave::create).thenReturn(mock(Enclave.class));

            //Payload publisher gubbins
            PayloadPublisherFactory payloadPublisherFactory = mock(PayloadPublisherFactory.class);
            when(payloadPublisherFactory.create(any(Config.class))).thenReturn(mock(PayloadPublisher.class));

            mockedStaticPayloadPublisherFactory.when(() -> PayloadPublisherFactory.newFactory(any(Config.class)))
                .thenReturn(payloadPublisherFactory);

            //BatchPayloadPublisherFactory gubbins
            BatchPayloadPublisherFactory batchPayloadPublisherFactory = mock(BatchPayloadPublisherFactory.class);
            when(batchPayloadPublisherFactory.create(any(PayloadPublisher.class))).thenReturn(mock(BatchPayloadPublisher.class));

            mockedStaticBatchPayloadPublisherFactory.when(BatchPayloadPublisherFactory::newFactory)
                .thenReturn(batchPayloadPublisherFactory);

            TransactionManagerFactory result = DefaultTransactionManagerFactory.INSTANCE;
            assertThat(result).isNotNull();

            Config config = mock(Config.class);
            ServerConfig serverConfig = mock(ServerConfig.class);
            when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
            when(config.getP2PServerConfig()).thenReturn(serverConfig);

            JdbcConfig jdbcConfig = mock(JdbcConfig.class);
            when(jdbcConfig.getUsername()).thenReturn("junit");
            when(jdbcConfig.getPassword()).thenReturn("junit");
            when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:junit");
            when(config.getJdbcConfig()).thenReturn(jdbcConfig);

            FeatureToggles features = mock(FeatureToggles.class);
            when(features.isEnablePrivacyEnhancements()).thenReturn(false);
            when(config.getFeatures()).thenReturn(features);

            TransactionManager transactionManager = result.create(config);
            assertThat(transactionManager).isNotNull();

            assertThat(result.create(config)).isSameAs(transactionManager);
            assertThat(result.transactionManager().get()).isSameAs(transactionManager);

        }
    }

}
