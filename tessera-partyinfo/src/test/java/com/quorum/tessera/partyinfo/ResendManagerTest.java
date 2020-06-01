package com.quorum.tessera.partyinfo;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
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
        this.payloadEncoder = mock(PayloadEncoder.class);
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

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(new ArrayList<>())
                        .build();

        final byte[] newEncryptedMasterKey = "newbox".getBytes();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());
        when(enclave.createNewRecipientBox(any(), any())).thenReturn(newEncryptedMasterKey);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(encodedPayload);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(senderKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(newEncryptedMasterKey);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).encode(any(EncodedPayload.class));
        verify(enclave).getPublicKeys();
        verify(enclave).createNewRecipientBox(any(), any());
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresent() {
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
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(encodedPayload);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox);

        verify(encryptedTransactionDAO).save(et);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(payloadEncoder).encode(existingEncodedPayload);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
        verify(enclave).unencryptTransaction(existingEncodedPayload, null);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentAndRecipientExisted() {
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
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(encodedPayload);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void messageMustContainManagedKeyAsSender() {
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

        final Throwable throwable = catchThrowable(() -> this.resendManager.acceptOwnMessage(encodedPayload));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message Q0lQSEVSVEVYVA== does not have one the nodes own keys as a sender");

        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
    }

    @Test
    public void invalidPayloadFromMaliciousRecipient() {
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
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());
        when(enclave.unencryptTransaction(existingEncodedPayload, null)).thenReturn("payload1".getBytes());

        final Throwable throwable = catchThrowable(() -> resendManager.acceptOwnMessage(encodedPayload));

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid payload provided");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, null);
        verify(enclave).unencryptTransaction(existingEncodedPayload, null);
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new ResendManagerImpl(encryptedTransactionDAO, enclave)).isNotNull();
    }
}
