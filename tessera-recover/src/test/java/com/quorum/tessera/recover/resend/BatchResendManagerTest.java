package com.quorum.tessera.recover.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.service.Service;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.util.Base64Codec;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Ignore
public class BatchResendManagerTest {

    private PayloadEncoder payloadEncoder;

    private Enclave enclave;

    private TransactionManager resendStoreDelegate;

    private StagingEntityDAO stagingEntityDAO;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private PartyInfoService partyInfoService;

    private BatchResendManager manager;

    private ResendBatchPublisher resendBatchPublisher;

    private static final String KEY_STRING = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";

    private final PublicKey publicKey = PublicKey.from(Base64Codec.create().decode(KEY_STRING));

    @Before
    public void init() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        resendStoreDelegate = mock(TransactionManager.class);
        stagingEntityDAO = mock(StagingEntityDAO.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        partyInfoService = mock(PartyInfoService.class);
        resendBatchPublisher = mock(ResendBatchPublisher.class);

        manager =
                new BatchResendManagerImpl(
                        payloadEncoder,
                        Base64Codec.create(),
                        enclave,
                        stagingEntityDAO,
                        encryptedTransactionDAO,
                        partyInfoService,resendBatchPublisher);

        when(enclave.status()).thenReturn(Service.Status.STARTED);

        final PublicKey publicKey =
                PublicKey.from(Base64Codec.create().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(payloadEncoder);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(resendStoreDelegate);
        verifyNoMoreInteractions(stagingEntityDAO);
        verifyNoMoreInteractions(encryptedTransactionDAO);
        verifyNoMoreInteractions(partyInfoService);
    }


    @Test
    public void doStuff() {
        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(3);
        request.setPublicKey(KEY_STRING);

        List<EncryptedTransaction> transactions =
            IntStream.range(0,1)
                .mapToObj(i -> mock(EncryptedTransaction.class))
                .collect(Collectors.toUnmodifiableList());

        when(encryptedTransactionDAO.transactionCount()).thenReturn(100L);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(),anyInt()))
            .thenReturn(transactions);

        ResendBatchResponse result = manager.resendBatch(request);

//        assertThat(result.getTotal())
//            .describedAs("total should be pouplatd from batchworkflow message count")
//            .isEqualTo(MockBatchWorkflow.PUBLISHED_COUNT);

        verify(encryptedTransactionDAO,times(34)).retrieveTransactions(anyInt(),anyInt());
        verify(encryptedTransactionDAO).transactionCount();
    }



    @Test
    public void createWithMinimalConstructor() {
        assertThat(new BatchResendManagerImpl(enclave, stagingEntityDAO, encryptedTransactionDAO, partyInfoService,resendBatchPublisher))
                .isNotNull();
    }

    @Test
    public void calculateBatchCount() {
        long total = 10;
        long batchSize = 3;

        int batchCount = BatchResendManagerImpl.calculateBatchCount(batchSize,total);

        assertThat(batchCount).isEqualTo(4);

    }

    @Test
    public void calculateBatchCountTotalLowerThanBatchSizeIsSingleBatch() {
        long total = 10;
        long batchSize = 15;

        int batchCount = BatchResendManagerImpl.calculateBatchCount(batchSize,total);

        assertThat(batchCount).isEqualTo(1);

    }

}
