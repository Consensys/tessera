package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ResendManagerTest {

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private PayloadEncoder payloadEncoder;

    private Enclave enclave;

    private ResendManager resendManager;

    @Before
    public void init() {
        this.encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        this.payloadEncoder = spy(PayloadEncoder.class);
        this.enclave = mock(Enclave.class);

        this.resendManager = new ResendManagerImpl(encryptedTransactionDAO, payloadEncoder, enclave);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(encryptedTransactionDAO, payloadEncoder, enclave);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsntPresent() {

        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final byte[] input = "SOMEDATA".getBytes();

        // A legacy payload has empty recipient and box
        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withCipherTextNonce(new Nonce("nonce".getBytes()))
                        .withRecipientBoxes(emptyList())
                        .withRecipientNonce(new Nonce("nonce".getBytes()))
                        .withRecipientKeys(emptyList())
                        .build();

        final byte[] newEncryptedMasterKey = "newbox".getBytes();

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());
        when(enclave.createNewRecipientBox(any(), any())).thenReturn(newEncryptedMasterKey);

        resendManager.acceptOwnMessage(input);

        ArgumentCaptor<EncodedPayload> payloadCapture = ArgumentCaptor.forClass(EncodedPayload.class);

        verify(payloadEncoder).encode(payloadCapture.capture());

        final EncodedPayload updatedPayload = payloadCapture.getValue();
        assertThat(updatedPayload).isNotNull();

        // The sender was added
        assertThat(updatedPayload.getRecipientKeys()).containsExactly(senderKey);

        // New box was created
        assertThat(updatedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(newEncryptedMasterKey));

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).createNewRecipientBox(any(), any());
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresent() {

        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(singletonList(recipientKey))
                        .build();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(new ArrayList<>())
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);
        when(payloadEncoder.decode(incomingData)).thenReturn(encodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(incomingData);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(recipientBox));

        verify(encryptedTransactionDAO).update(et);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(payloadEncoder).decode(incomingData);
        verify(payloadEncoder).encode(any(EncodedPayload.class));
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
        verify(enclave).unencryptTransaction(existingEncodedPayload, null);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentAndRecipientExisted() {

        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(singletonList(recipientKey))
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(encodedPayload);
        when(payloadEncoder.decode(incomingData)).thenReturn(encodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(incomingData);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(recipientBox));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(payloadEncoder).decode(incomingData);
        verify(payloadEncoder).encode(any(EncodedPayload.class));
        verify(enclave).getPublicKeys();
        verify(enclave, times(2)).unencryptTransaction(encodedPayload, null);
        verify(encryptedTransactionDAO).update(et);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentPrivacyModeIsPSVAndRecipientsDifferThrows() {

        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final PublicKey anotherRecipientKey = PublicKey.from("ANOTHER-RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(Arrays.asList(recipientKey, anotherRecipientKey))
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withAffectedContractTransactions(new HashMap<>())
                        .withExecHash(new byte[0])
                        .build();

        final EncodedPayload incomingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(singletonList(anotherRecipientKey))
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withAffectedContractTransactions(new HashMap<>())
                        .withExecHash(new byte[0])
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn("data".getBytes());
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);
        when(payloadEncoder.decode(incomingData)).thenReturn(incomingEncodedPayload);

        final Throwable throwable = catchThrowable(() -> this.resendManager.acceptOwnMessage(incomingData));

        assertThat(throwable)
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("Participants mismatch for two versions of transaction");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(payloadEncoder).decode(incomingData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(existingEncodedPayload, null);
        verify(enclave).unencryptTransaction(incomingEncodedPayload, null);
    }

    @Test
    public void messageMustContainManagedKeyAsSender() {
        final byte[] incomingData = "incomingData".getBytes();

        final PublicKey senderKey = PublicKey.from("SENDER_WHO_ISNT_US".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(singletonList(recipientKey))
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(PublicKey.from("OTHER".getBytes())));
        when(payloadEncoder.decode(incomingData)).thenReturn(encodedPayload);

        final Throwable throwable = catchThrowable(() -> this.resendManager.acceptOwnMessage(incomingData));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message Q0lQSEVSVEVYVA== does not have one the nodes own keys as a sender");

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).decode(incomingData);
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void invalidPayloadFromMaliciousRecipient() {
        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox))
                        .withRecipientKeys(singletonList(recipientKey))
                        .build();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(new ArrayList<>())
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);
        when(payloadEncoder.decode(incomingData)).thenReturn(encodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());
        when(enclave.unencryptTransaction(existingEncodedPayload, null)).thenReturn("payload1".getBytes());

        final Throwable throwable = catchThrowable(() -> resendManager.acceptOwnMessage(incomingData));

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid payload provided");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(payloadEncoder).decode(incomingData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
        verify(enclave).unencryptTransaction(existingEncodedPayload, null);
    }

    @Test
    public void undecryptablePayloadErrors() {
        final byte[] incomingData = "incomingData".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(mock(PublicKey.class))
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(emptyList())
                        .withRecipientKeys(emptyList())
                        .build();

        when(payloadEncoder.decode(incomingData)).thenReturn(encodedPayload);
        when(enclave.unencryptTransaction(encodedPayload, null)).thenThrow(IllegalArgumentException.class);

        final Throwable throwable = catchThrowable(() -> resendManager.acceptOwnMessage(incomingData));

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage(null);

        verify(payloadEncoder).decode(incomingData);
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void constructWithMinimalArgs() {

        assertThat(new ResendManagerImpl(encryptedTransactionDAO, enclave)).isNotNull();
    }
}
