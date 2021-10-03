package com.quorum.tessera.recovery.workflow.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendResponse;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LegacyResendManagerImplTest {

  private Enclave enclave;

  private Discovery discovery;

  private PayloadEncoder encoder;

  private PayloadPublisher publisher;

  private EncryptedTransactionDAO dao;

  private LegacyResendManager resendManager;

  @Before
  public void init() {
    this.enclave = mock(Enclave.class);
    this.discovery = mock(Discovery.class);
    this.encoder = mock(PayloadEncoder.class);
    this.publisher = mock(PayloadPublisher.class);
    this.dao = mock(EncryptedTransactionDAO.class);

    this.resendManager =
        new LegacyResendManagerImpl(enclave, dao, 1, encoder, publisher, discovery);
  }

  @After
  public void after() {
    verifyNoMoreInteractions(enclave, discovery, encoder, publisher, dao);
  }

  @Test
  public void individualMissingTxFails() {
    when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

    final MessageHash txHash = new MessageHash("sample-hash".getBytes());
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final ResendRequest request =
        ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

    final Throwable throwable = catchThrowable(() -> resendManager.resend(request));

    assertThat(throwable)
        .isInstanceOf(TransactionNotFoundException.class)
        .hasMessage("Message with hash c2FtcGxlLWhhc2g= was not found");

    verify(dao).retrieveByHash(txHash);
  }

  @Test
  public void individualNonStandardPrivateTxFails() {
    final EncodedPayload nonSPPayload = mock(EncodedPayload.class);
    when(nonSPPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);

    final EncryptedTransaction databaseTx = mock(EncryptedTransaction.class);
    when(databaseTx.getPayload()).thenReturn(nonSPPayload);
    when(databaseTx.getEncodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);

    when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));

    final MessageHash txHash = new MessageHash("sample-hash".getBytes());
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final ResendRequest request =
        ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

    final Throwable throwable = catchThrowable(() -> resendManager.resend(request));

    assertThat(throwable)
        .isInstanceOf(EnhancedPrivacyNotSupportedException.class)
        .hasMessage("Cannot resend enhanced privacy transaction in legacy resend");

    verify(dao).retrieveByHash(txHash);
  }

  @Test
  public void targetKeyIsNotSenderOfTransaction() {
    final MessageHash txHash = new MessageHash("sample-hash".getBytes());
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());

    final EncodedPayload nonSPPayload = mock(EncodedPayload.class);
    when(nonSPPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

    final EncryptedTransaction databaseTx = mock(EncryptedTransaction.class);
    when(databaseTx.getPayload()).thenReturn(nonSPPayload);
    when(databaseTx.getEncodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);

    final ResendRequest request =
        ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

    when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));
    when(encoder.forRecipient(nonSPPayload, targetResendKey)).thenReturn(nonSPPayload);

    final ResendResponse response = resendManager.resend(request);

    assertThat(response).isNotNull();
    assertThat(response.getPayload()).isEqualTo(nonSPPayload);

    verify(dao).retrieveByHash(txHash);
    verify(encoder).forRecipient(nonSPPayload, targetResendKey);
  }

  @Test
  public void targetIsSenderOfTransaction() {
    final MessageHash txHash = new MessageHash("sample-hash".getBytes());
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final PublicKey localRecipientKey = PublicKey.from("local-recipient".getBytes());

    final EncodedPayload nonSPPayload = mock(EncodedPayload.class);
    when(nonSPPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(nonSPPayload.getSenderKey()).thenReturn(targetResendKey);

    RecipientBox recipientBox = mock(RecipientBox.class);
    when(recipientBox.getData()).thenReturn("testBox".getBytes());
    when(nonSPPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox));

    final EncryptedTransaction databaseTx = mock(EncryptedTransaction.class);
    when(databaseTx.getEncodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);
    when(databaseTx.getPayload()).thenReturn(nonSPPayload);

    databaseTx.setEncodedPayloadCodec(EncodedPayloadCodec.LEGACY);

    final ResendRequest request =
        ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
            .withHash(txHash)
            .withRecipient(targetResendKey)
            .build();

    when(dao.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(databaseTx));
    when(enclave.getPublicKeys()).thenReturn(Set.of(localRecipientKey));
    when(enclave.unencryptTransaction(any(), eq(localRecipientKey))).thenReturn(new byte[0]);

    final ResendResponse response = resendManager.resend(request);

    assertThat(response).isNotNull();
    EncodedPayload resultingEncodedPayload = response.getPayload();
    assertThat(resultingEncodedPayload.getRecipientKeys()).containsExactly(localRecipientKey);

    verify(dao).retrieveByHash(txHash);
    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(any(), eq(localRecipientKey));
  }

  @Test
  public void performResendAll() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final ResendRequest request =
        ResendRequest.Builder.create()
            .withType(ResendRequest.ResendRequestType.ALL)
            .withRecipient(targetResendKey)
            .build();

    // Not bothered about going through the process, just make sure they are all loaded from the
    // database
    // We are not testing the workflow itself, only that the workflow gets the right amount of
    // transactions

    when(dao.transactionCount()).thenReturn(2L);
    when(dao.retrieveTransactions(0, 1)).thenReturn(List.of(new EncryptedTransaction()));
    when(dao.retrieveTransactions(1, 1)).thenReturn(List.of(new EncryptedTransaction()));

    final ResendResponse response = resendManager.resend(request);
    assertThat(response).isNotNull();
    assertThat(response.getPayload()).isNull();

    verify(enclave, times(2)).status();
    verify(dao).transactionCount();
    verify(dao).retrieveTransactions(0, 1);
    verify(dao).retrieveTransactions(1, 1);
  }
}
