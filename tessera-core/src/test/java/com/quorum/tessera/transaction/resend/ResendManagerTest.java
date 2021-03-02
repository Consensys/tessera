package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.enclave.PayloadDigest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ResendManagerTest {

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private PayloadEncoder payloadEncoder;

    private Enclave enclave;

    private PayloadDigest payloadDigest;

    private ResendManager resendManager;

    @Before
    public void init() {
        this.encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.enclave = mock(Enclave.class);
        payloadDigest = cipherText -> cipherText;

        this.resendManager = new ResendManagerImpl(encryptedTransactionDAO, payloadEncoder, enclave, payloadDigest);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(encryptedTransactionDAO, payloadEncoder, enclave);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsntPresent() {
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

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

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());
        when(enclave.createNewRecipientBox(any(), any())).thenReturn(newEncryptedMasterKey);

        resendManager.acceptOwnMessage(encodedPayload);

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
        verify(enclave).getPublicKeys();
        verify(enclave).createNewRecipientBox(any(), any());
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresent() {
        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
        final byte[] recipientBox1 = "BOX1".getBytes();
        final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
        final byte[] recipientBox2 = "BOX2".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox2))
                        .withRecipientKeys(singletonList(recipientKey2))
                        .build();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox1))
                        .withRecipientKeys(singletonList(recipientKey1))
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(encodedPayload);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey2);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(recipientBox2));

        ArgumentCaptor<EncodedPayload> updatedPayload = ArgumentCaptor.forClass(EncodedPayload.class);

        verify(payloadEncoder).encode(updatedPayload.capture());

        final EncodedPayload updated = updatedPayload.getValue();

        // Check recipients are being added
        assertThat(updated.getRecipientKeys()).hasSize(2).containsExactlyInAnyOrder(recipientKey1, recipientKey2);

        // Check boxes are being added
        assertThat(updated.getRecipientBoxes()).hasSize(2);

        verify(encryptedTransactionDAO).update(et);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
        verify(enclave).unencryptTransaction(existingEncodedPayload, senderKey);
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentAndPsv() {
        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
        final byte[] recipientBox1 = "BOX1".getBytes();
        final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
        final byte[] recipientBox2 = "BOX2".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withExecHash("execHash".getBytes())
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox2))
                        .withRecipientKeys(List.of(recipientKey2, senderKey))
                        .build();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(singletonList(recipientBox1))
                        .withRecipientKeys(singletonList(recipientKey1))
                        .build();

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn("updated".getBytes());

        resendManager.acceptOwnMessage(encodedPayload);

        ArgumentCaptor<EncodedPayload> updatedPayload = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(payloadEncoder).encode(updatedPayload.capture());
        final EncodedPayload updated = updatedPayload.getValue();

        // Check recipients are being added
        assertThat(updated.getRecipientKeys()).containsExactlyInAnyOrder(recipientKey1, recipientKey2);

        // Check boxes are being added
        assertThat(updated.getRecipientBoxes()).hasSize(2);

        verify(encryptedTransactionDAO).update(et);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), eq(senderKey));
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentAndRecipientAlreadyExists() {
        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey1 = PublicKey.from("RECIPIENT-KEY1".getBytes());
        final byte[] recipientBox1 = "BOX1".getBytes();
        final PublicKey recipientKey2 = PublicKey.from("RECIPIENT-KEY2".getBytes());
        final byte[] recipientBox2 = "BOX2".getBytes();

        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(List.of(recipientBox2))
                        .withRecipientKeys(List.of(recipientKey2))
                        .build();

        final EncodedPayload existingEncodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(List.of(recipientBox1, recipientBox2))
                        .withRecipientKeys(List.of(recipientKey1, recipientKey2))
                        .build();

        when(enclave.getPublicKeys()).thenReturn(Set.of(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decode(storedData)).thenReturn(existingEncodedPayload);

        resendManager.acceptOwnMessage(encodedPayload);

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey2);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(recipientBox2));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
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
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(RecipientBox.from(recipientBox));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
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
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
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
        when(enclave.unencryptTransaction(existingEncodedPayload, senderKey)).thenReturn("payload1".getBytes());

        final Throwable throwable = catchThrowable(() -> resendManager.acceptOwnMessage(encodedPayload));

        assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid payload provided");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(storedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayload, senderKey);
        verify(enclave).unencryptTransaction(existingEncodedPayload, senderKey);
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new ResendManagerImpl(encryptedTransactionDAO, enclave, payloadDigest)).isNotNull();
    }

    @Test
    public void createFromServiceLoader() {

        ServiceLoader<ResendManager> serviceLoader = mock(ServiceLoader.class);

        ResendManager resendManager = mock(ResendManager.class);
        when(serviceLoader.findFirst()).thenReturn(Optional.of(resendManager));

        final ResendManager result;
        try(var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(ResendManager.class)).thenReturn(serviceLoader);

            result = ResendManager.create();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(ResendManager.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();

        }

        assertThat(result).isSameAs(resendManager);
        verify(serviceLoader).findFirst();
        verifyNoMoreInteractions(serviceLoader);
        verifyNoInteractions(resendManager);

    }



}
