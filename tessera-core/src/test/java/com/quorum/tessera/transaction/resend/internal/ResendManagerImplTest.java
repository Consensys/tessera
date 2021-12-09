package com.quorum.tessera.transaction.resend.internal;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.resend.ResendManager;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ResendManagerImplTest {

  private EncryptedTransactionDAO encryptedTransactionDAO;

  private Enclave enclave;

  private ResendManager resendManager;

  final PublicKey senderKey = PublicKey.from("SENDER".getBytes());
  final byte[] cipherText = "CIPHERTEXT".getBytes();
  final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
  final RecipientBox recipientBox1 = RecipientBox.from("BOX1".getBytes());
  final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
  final RecipientBox recipientBox2 = RecipientBox.from("BOX2".getBytes());

  @Before
  public void init() {
    this.encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
    this.enclave = mock(Enclave.class);
    PayloadDigest payloadDigest = cipherText -> cipherText;

    this.resendManager = new ResendManagerImpl(encryptedTransactionDAO, enclave, payloadDigest);
  }

  @After
  public void after() {
    verifyNoMoreInteractions(encryptedTransactionDAO, enclave);
  }

  @Test
  public void storePayloadAsSenderWhenTxIsNotPresent() {
    final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

    // A legacy payload has empty recipient and box
    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);

    final byte[] newEncryptedMasterKey = "newbox".getBytes();

    when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.empty());
    when(enclave.createNewRecipientBox(any(), any())).thenReturn(newEncryptedMasterKey);

    resendManager.acceptOwnMessage(encodedPayload);

    ArgumentCaptor<EncryptedTransaction> updatedTxCaptor =
        ArgumentCaptor.forClass(EncryptedTransaction.class);

    verify(encryptedTransactionDAO).save(updatedTxCaptor.capture());

    final EncodedPayload updatedPayload = updatedTxCaptor.getValue().getPayload();
    assertThat(updatedPayload).isNotNull();

    // The sender was added
    assertThat(updatedPayload.getRecipientKeys()).containsExactly(senderKey);

    // New box was created
    assertThat(updatedPayload.getRecipientBoxes())
        .containsExactly(RecipientBox.from(newEncryptedMasterKey));

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave).createNewRecipientBox(any(), any());
    verify(enclave).unencryptTransaction(encodedPayload, senderKey);
  }

  @Test
  public void storePayloadAsSenderWhenTxIsPresent() {

    final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

    final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
    final RecipientBox recipientBox1 = RecipientBox.from("BOX1".getBytes());
    final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
    final RecipientBox recipientBox2 = RecipientBox.from("BOX2".getBytes());

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey2));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox2));

    final EncodedPayload existingEncodedPayload = mock(EncodedPayload.class);
    when(existingEncodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(existingEncodedPayload.getSenderKey()).thenReturn(senderKey);
    when(existingEncodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey1));
    when(existingEncodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox1));

    final EncryptedTransaction et =
        new EncryptedTransaction(mock(MessageHash.class), existingEncodedPayload);

    when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.of(et));

    resendManager.acceptOwnMessage(encodedPayload);

    assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey2);
    assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox2);

    ArgumentCaptor<EncryptedTransaction> updatedTxCaptor =
        ArgumentCaptor.forClass(EncryptedTransaction.class);
    verify(encryptedTransactionDAO).update(updatedTxCaptor.capture());

    final EncodedPayload updated = updatedTxCaptor.getValue().getPayload();

    // Check recipients are being added
    assertThat(updated.getRecipientKeys())
        .hasSize(2)
        .containsExactlyInAnyOrder(recipientKey1, recipientKey2);

    // Check boxes are being added
    assertThat(updated.getRecipientBoxes()).hasSize(2);

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(encodedPayload, senderKey);
    verify(enclave).unencryptTransaction(existingEncodedPayload, senderKey);
  }

  @Test
  public void storePayloadAsSenderWhenTxIsPresentAndPsv() {

    final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

    final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
    final RecipientBox recipientBox1 = RecipientBox.from("BOX1".getBytes());
    final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
    final RecipientBox recipientBox2 = RecipientBox.from("BOX2".getBytes());

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey2, senderKey));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox2));
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

    final EncodedPayload existingEncodedPayload = mock(EncodedPayload.class);
    when(existingEncodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(existingEncodedPayload.getSenderKey()).thenReturn(senderKey);
    when(existingEncodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey1));
    when(existingEncodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox1));

    final EncryptedTransaction et =
        new EncryptedTransaction(mock(MessageHash.class), existingEncodedPayload);

    when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.of(et));

    resendManager.acceptOwnMessage(encodedPayload);

    ArgumentCaptor<EncryptedTransaction> updatedTxCaptor =
        ArgumentCaptor.forClass(EncryptedTransaction.class);

    verify(encryptedTransactionDAO).update(updatedTxCaptor.capture());

    final EncodedPayload updated = updatedTxCaptor.getValue().getPayload();

    // Check recipients are being added
    assertThat(updated.getRecipientKeys()).containsExactlyInAnyOrder(recipientKey1, recipientKey2);

    // Check boxes are being added
    assertThat(updated.getRecipientBoxes()).hasSize(2);

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), eq(senderKey));
  }

  @Test
  public void storePayloadAsSenderWhenTxIsPresentAndRecipientAlreadyExists() {

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey2));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox2));

    final EncodedPayload existingEncodedPayload = mock(EncodedPayload.class);
    when(existingEncodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(existingEncodedPayload.getSenderKey()).thenReturn(senderKey);
    when(existingEncodedPayload.getRecipientKeys())
        .thenReturn(List.of(recipientKey1, recipientKey2));
    when(existingEncodedPayload.getRecipientBoxes())
        .thenReturn(List.of(recipientBox1, recipientBox2));

    final EncryptedTransaction et = mock(EncryptedTransaction.class);
    when(et.getPayload()).thenReturn(existingEncodedPayload);

    when(enclave.getPublicKeys()).thenReturn(Set.of(senderKey));
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.of(et));

    resendManager.acceptOwnMessage(encodedPayload);

    assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey2);
    assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox2);

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(encodedPayload, senderKey);
  }

  @Test
  public void storePayloadAsSenderWhenTxIsPresentAndRecipientExisted() {

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);
    when(encodedPayload.getCipherText()).thenReturn(cipherText);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey1));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox1));

    when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

    final EncryptedTransaction et = mock(EncryptedTransaction.class);
    when(et.getPayload()).thenReturn(encodedPayload);
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.of(et));

    resendManager.acceptOwnMessage(encodedPayload);

    assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey1);
    assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox1);

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(encodedPayload, senderKey);
  }

  @Test
  public void messageMustContainManagedKeyAsSender() {
    final PublicKey someSender = PublicKey.from("SENDER_WHO_ISNT_US".getBytes());

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(someSender);
    when(encodedPayload.getCipherText()).thenReturn(cipherText);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey1));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox1));

    when(enclave.getPublicKeys()).thenReturn(singleton(PublicKey.from("OTHER".getBytes())));

    final Throwable throwable =
        catchThrowable(() -> this.resendManager.acceptOwnMessage(encodedPayload));

    assertThat(throwable)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Message Q0lQSEVSVEVYVA== does not have one the nodes own keys as a sender");

    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(encodedPayload, someSender);
  }

  @Test
  public void invalidPayloadFromMaliciousRecipient() {

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(senderKey);
    when(encodedPayload.getCipherText()).thenReturn(cipherText);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey1));
    when(encodedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox1));

    final EncodedPayload existingEncodedPayload = mock(EncodedPayload.class);
    when(existingEncodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
    when(existingEncodedPayload.getSenderKey()).thenReturn(senderKey);
    when(existingEncodedPayload.getRecipientKeys()).thenReturn(List.of());
    when(existingEncodedPayload.getRecipientBoxes()).thenReturn(List.of());

    final EncryptedTransaction et = mock(EncryptedTransaction.class);
    when(et.getPayload()).thenReturn(existingEncodedPayload);

    when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
    when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
        .thenReturn(Optional.of(et));

    when(enclave.unencryptTransaction(existingEncodedPayload, senderKey))
        .thenReturn("payload1".getBytes());

    final Throwable throwable =
        catchThrowable(() -> resendManager.acceptOwnMessage(encodedPayload));

    assertThat(throwable)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid payload provided");

    verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    verify(enclave).getPublicKeys();
    verify(enclave).unencryptTransaction(encodedPayload, senderKey);
    verify(enclave).unencryptTransaction(existingEncodedPayload, senderKey);
  }

  @Test
  public void constructWithMinimalArgs() {
    assertThat(new ResendManagerImpl(encryptedTransactionDAO, enclave, mock(PayloadDigest.class)))
        .isNotNull();
  }
}
