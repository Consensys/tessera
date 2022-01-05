package com.quorum.tessera.recovery.workflow.internal;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.gt;
import static org.mockito.AdditionalMatchers.lt;
import static org.mockito.Mockito.*;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.PushBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.BatchWorkflow;
import com.quorum.tessera.recovery.workflow.BatchWorkflowContext;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BatchResendManagerImplTest {

  private PayloadEncoder payloadEncoder;

  private StagingEntityDAO stagingEntityDAO;

  private EncryptedTransactionDAO encryptedTransactionDAO;

  private BatchResendManager manager;

  private static final String KEY_STRING = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";

  private final PublicKey publicKey = PublicKey.from(Base64Codec.create().decode(KEY_STRING));

  private BatchWorkflowFactory batchWorkflowFactory;

  @Before
  public void beforeTest() {
    payloadEncoder = mock(PayloadEncoder.class);
    stagingEntityDAO = mock(StagingEntityDAO.class);
    encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
    batchWorkflowFactory = mock(BatchWorkflowFactory.class);

    manager =
        new BatchResendManagerImpl(
            stagingEntityDAO, encryptedTransactionDAO, 5, batchWorkflowFactory);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(payloadEncoder);
    verifyNoMoreInteractions(stagingEntityDAO);
    verifyNoMoreInteractions(encryptedTransactionDAO);
    verifyNoMoreInteractions(batchWorkflowFactory);
  }

  @Test
  public void resendBatch() {

    ResendBatchRequest request =
        ResendBatchRequest.Builder.create().withBatchSize(3).withPublicKey(KEY_STRING).build();

    List<EncryptedTransaction> transactions =
        IntStream.range(0, 5)
            .mapToObj(i -> mock(EncryptedTransaction.class))
            .collect(Collectors.toUnmodifiableList());

    when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);

    when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
    when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
        .thenReturn(singletonList(mock(EncryptedTransaction.class)));

    BatchWorkflow batchWorkflow = mock(BatchWorkflow.class);
    when(batchWorkflow.getPublishedMessageCount()).thenReturn(999L);

    when(batchWorkflowFactory.create(101L)).thenReturn(batchWorkflow);

    final ResendBatchResponse result = manager.resendBatch(request);

    assertThat(result.getTotal()).isEqualTo(999L);
    verify(batchWorkflow).getPublishedMessageCount();

    verify(batchWorkflow, times(101)).execute(any(BatchWorkflowContext.class));

    verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());

    verify(encryptedTransactionDAO).transactionCount();

    verify(batchWorkflowFactory).create(101L);
  }

  @Test
  public void useMaxResultsWhenBatchSizeNotProvided() {

    final ResendBatchRequest request =
        ResendBatchRequest.Builder.create().withPublicKey(KEY_STRING).build();

    List<EncryptedTransaction> transactions =
        IntStream.range(0, 5)
            .mapToObj(i -> mock(EncryptedTransaction.class))
            .collect(Collectors.toUnmodifiableList());

    when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);

    BatchWorkflow batchWorkflow = mock(BatchWorkflow.class);

    when(batchWorkflow.getPublishedMessageCount())
        .thenReturn(999L); // arbitary total that's returned as result.getTotal()

    when(batchWorkflowFactory.create(101L)).thenReturn(batchWorkflow);

    when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
    when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
        .thenReturn(List.of(mock(EncryptedTransaction.class)));

    final ResendBatchResponse result = manager.resendBatch(request);

    assertThat(result.getTotal()).isEqualTo(999L);

    verify(batchWorkflow, times(101)).execute(any(BatchWorkflowContext.class));

    verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());
    verify(encryptedTransactionDAO).transactionCount();

    verify(batchWorkflowFactory).create(101L);
  }

  @Test
  public void useMaxResultsAlsoWhenBatchSizeTooLarge() {

    final ResendBatchRequest request =
        ResendBatchRequest.Builder.create()
            .withBatchSize(10000000)
            .withPublicKey(KEY_STRING)
            .build();

    List<EncryptedTransaction> transactions =
        IntStream.range(0, 5)
            .mapToObj(i -> mock(EncryptedTransaction.class))
            .collect(Collectors.toUnmodifiableList());

    when(encryptedTransactionDAO.transactionCount()).thenReturn(101L);

    when(encryptedTransactionDAO.retrieveTransactions(lt(100), anyInt())).thenReturn(transactions);
    when(encryptedTransactionDAO.retrieveTransactions(gt(99), anyInt()))
        .thenReturn(singletonList(mock(EncryptedTransaction.class)));

    final BatchWorkflow batchWorkflow = mock(BatchWorkflow.class);
    when(batchWorkflow.getPublishedMessageCount()).thenReturn(999L);
    when(batchWorkflowFactory.create(101L)).thenReturn(batchWorkflow);

    final ResendBatchResponse result = manager.resendBatch(request);
    assertThat(result.getTotal()).isEqualTo(999L);

    verify(batchWorkflow, times(101)).execute(any(BatchWorkflowContext.class));

    verify(encryptedTransactionDAO, times(21)).retrieveTransactions(anyInt(), anyInt());

    verify(encryptedTransactionDAO).transactionCount();

    verify(batchWorkflowFactory).create(101L);
  }

  @Test
  public void createWithMinimalConstructor() {
    assertThat(
            new BatchResendManagerImpl(
                stagingEntityDAO, encryptedTransactionDAO, 1, mock(BatchWorkflowFactory.class)))
        .isNotNull();
  }

  @Test
  public void calculateBatchCount() {
    long numberOfRecords = 10;
    long maxResults = 3;

    int batchCount = BatchResendManagerImpl.calculateBatchCount(maxResults, numberOfRecords);

    assertThat(batchCount).isEqualTo(4);
  }

  @Test
  public void calculateBatchCountTotalLowerThanBatchSizeIsSingleBatch() {
    long numberOfRecords = 100;
    long maxResults = 10;

    int batchCount = BatchResendManagerImpl.calculateBatchCount(maxResults, numberOfRecords);

    assertThat(batchCount).isEqualTo(10);
  }

  @Test
  public void createBatchResendManager() {
    BatchResendManager expected = mock(BatchResendManager.class);
    BatchResendManager result;
    try (var staticServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader<BatchResendManager> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      staticServiceLoader
          .when(() -> ServiceLoader.load(BatchResendManager.class))
          .thenReturn(serviceLoader);
      result = BatchResendManager.create();

      staticServiceLoader.verify(() -> ServiceLoader.load(BatchResendManager.class));
      staticServiceLoader.verifyNoMoreInteractions();
      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }
    assertThat(result).isNotNull().isSameAs(expected);
  }

  @Test
  public void testStoreResendBatchMultipleVersions() {

    try (var payloadDigestMockedStatic = mockStatic(PayloadDigest.class);
        var payloadEncoderMockedStatic = mockStatic(PayloadEncoder.class)) {

      payloadDigestMockedStatic
          .when(PayloadDigest::create)
          .thenReturn((PayloadDigest) cipherText -> cipherText);

      payloadEncoderMockedStatic
          .when(() -> PayloadEncoder.create(any()))
          .thenReturn(payloadEncoder);

      final EncodedPayload encodedPayload =
          EncodedPayload.Builder.create()
              .withSenderKey(publicKey)
              .withCipherText("cipherText".getBytes())
              .withCipherTextNonce(new Nonce("nonce".getBytes()))
              .withRecipientBoxes(singletonList("box".getBytes()))
              .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
              .withRecipientKeys(singletonList(PublicKey.from("receiverKey".getBytes())))
              .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
              .withAffectedContractTransactions(emptyMap())
              .withExecHash(new byte[0])
              .build();

      when(payloadEncoder.decode(any())).thenReturn(encodedPayload);

      final byte[] raw = new PayloadEncoderImpl().encode(encodedPayload);

      PushBatchRequest request = PushBatchRequest.from(List.of(raw), EncodedPayloadCodec.LEGACY);

      StagingTransaction existing = new StagingTransaction();

      when(stagingEntityDAO.retrieveByHash(any())).thenReturn(Optional.of(existing));
      when(stagingEntityDAO.update(any(StagingTransaction.class)))
          .thenReturn(new StagingTransaction());

      manager.storeResendBatch(request);

      verify(stagingEntityDAO).save(any(StagingTransaction.class));
      verify(payloadEncoder).decode(any());
      verify(payloadEncoder).encodedPayloadCodec();
      payloadDigestMockedStatic.verify(PayloadDigest::create);
      payloadDigestMockedStatic.verifyNoMoreInteractions();
    }
  }
}
