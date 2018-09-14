package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import org.assertj.core.api.Assertions;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private static final Key RECIPIENT_KEY = new Key(new byte[]{-1, -2, -3});

    private static final Key SENDER_KEY = new Key(new byte[]{1, 2, 3});

    private static final byte[] CIPHER_TEXT = new byte[]{4, 5, 6};

    private static final Nonce NONCE = new Nonce(new byte[]{7, 8, 9});

    private static final Nonce RECIPIENT_NONCE = new Nonce(new byte[]{10, 11, 12});

    private static final byte[] RECIPIENT_BOX = "RECIPIENT_BOX".getBytes();

    private EncryptedTransactionDAO dao;

    private PayloadEncoder payloadEncoder;

    private KeyManager keyManager;

    private NaclFacade naclFacade;

    private TransactionService transactionService;

    @Before
    public void init() {
        this.dao = mock(EncryptedTransactionDAO.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.keyManager = mock(KeyManager.class);
        this.naclFacade = mock(NaclFacade.class);

        this.transactionService = new TransactionServiceImpl(dao, payloadEncoder, keyManager, naclFacade);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(dao, keyManager, naclFacade, payloadEncoder);
    }

    @Test
    public void deleteDelegatesToDao() {

        transactionService.delete(new MessageHash(new byte[0]));

        verify(dao).delete(any());

    }

    @Test
    public void retrieveAllReturnsEmptyIfKeyIsNull() {

        byte[] keyData = UUID.randomUUID().toString().getBytes();

        byte[] encodedPayload = UUID.randomUUID().toString().getBytes();

        EncryptedTransaction encTx = mock(EncryptedTransaction.class);
        when(encTx.getEncodedPayload()).thenReturn(encodedPayload);

        EncodedPayloadWithRecipients encodedPayloadWithRecipientz = mock(EncodedPayloadWithRecipients.class);

        Key key = new Key(keyData);
        when(encodedPayloadWithRecipientz.getRecipientKeys()).thenReturn(singletonList(key));

        when(dao.retrieveAllTransactions()).thenReturn(singletonList(encTx));

        when(payloadEncoder.decodePayloadWithRecipients(any())).thenReturn(encodedPayloadWithRecipientz);

        final Collection<EncodedPayloadWithRecipients> encodedPayloadWithRecipients
                = transactionService.retrieveAllForRecipient(null);

        assertThat(encodedPayloadWithRecipients).hasSize(0);
        verify(dao).retrieveAllTransactions();
        verify(payloadEncoder).decodePayloadWithRecipients(encodedPayload);
    }


    @Test
    public void storingPayloadCalculatesSha3512Hash() {

        byte[] data = UUID.randomUUID().toString().getBytes();

        EncodedPayloadWithRecipients payload
                = mock(EncodedPayloadWithRecipients.class);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getCipherText()).thenReturn(data);
        when(payload.getEncodedPayload()).thenReturn(encodedPayload);

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] expectedDigest = digestSHA3.digest(data);

        final MessageHash hash = transactionService.storeEncodedPayload(payload);

        assertThat(hash.getHashBytes()).isEqualTo(expectedDigest);
        verify(dao).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).encode(payload);

    }

    @Test
    public void messageGetsEncryptedUsingRandomKey() {

        final byte[] message = "MESSAGE".getBytes();
        final Key masterKey = new Key("MASTER".getBytes());
        final Key privateKey = new Key("PRIVATE".getBytes());
        final Key sharedKey = new Key("SHARED".getBytes());

        doReturn(privateKey).when(keyManager).getPrivateKeyForPublicKey(SENDER_KEY);

        when(naclFacade.randomNonce()).thenReturn(new Nonce(new byte[]{1}), new Nonce(new byte[]{2}));
        doReturn(masterKey).when(naclFacade).createSingleKey();
        doReturn(sharedKey).when(naclFacade).computeSharedKey(RECIPIENT_KEY, privateKey);

        when(naclFacade.sealAfterPrecomputation(any(), any(), any()))
                .thenReturn("CIPHER_MAIN".getBytes(), "CIPHER_MASTER".getBytes());

        final EncodedPayloadWithRecipients payloadWithRecipients
                = transactionService.encryptPayload(message, SENDER_KEY, singletonList(RECIPIENT_KEY));

        assertThat(payloadWithRecipients.getRecipientKeys()).hasSize(1);
        assertThat(payloadWithRecipients.getRecipientKeys()).containsExactly(RECIPIENT_KEY);

        final EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        assertThat(encodedPayload.getSenderKey()).isEqualTo(SENDER_KEY);
        assertThat(encodedPayload.getCipherText()).isEqualTo("CIPHER_MAIN".getBytes());
        Assertions.assertThat(encodedPayload.getCipherTextNonce()).isEqualTo(new Nonce(new byte[]{1}));
        assertThat(encodedPayload.getRecipientBoxes()).hasSize(1);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly("CIPHER_MASTER".getBytes());
        Assertions.assertThat(encodedPayload.getRecipientNonce()).isEqualTo(new Nonce(new byte[]{2}));

        verify(naclFacade).createSingleKey();
        verify(naclFacade, times(2)).randomNonce();
        verify(naclFacade, times(2)).sealAfterPrecomputation(any(), any(), any());
        verify(naclFacade).computeSharedKey(any(), any());
        verify(keyManager).getPrivateKeyForPublicKey(SENDER_KEY);

    }

    @Test
    public void unencryptMessageThrowsExceptionWhenMessageNotFound() {

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Key key = new Key(new byte[]{11, 12, 13, 14});

        doReturn(Optional.empty()).when(dao).retrieveByHash(hash);

        final Throwable throwable = catchThrowable(() -> transactionService.retrieveUnencryptedTransaction(hash, key));

        assertThat(throwable)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Message with hash " + hash + " was not found");

        verify(dao).retrieveByHash(hash);

    }

    @Test
    public void unencryptMessageUsesKeyAsSenderKey() {

        final Key senderKey = new Key("SENDER_KEY".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList(RECIPIENT_BOX));
        when(encodedPayload.getRecipientNonce()).thenReturn(RECIPIENT_NONCE);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn(CIPHER_TEXT);
        when(encodedPayload.getCipherTextNonce()).thenReturn(NONCE);

        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});

        final Key key = new Key(new byte[]{11, 12, 13, 14});

        final Key privateKey = new Key("PRIVATE_KEY".getBytes());

        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        final byte[] txnData = UUID.randomUUID().toString().getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(hash, txnData);

        final EncodedPayloadWithRecipients payloadWithRecipients = new EncodedPayloadWithRecipients(
                encodedPayload,
                emptyList()
        );

        when(payloadEncoder.decodePayloadWithRecipients(txnData))
                .thenReturn(payloadWithRecipients);

        when(dao.retrieveByHash(hash)).thenReturn(Optional.of(tx));

        when(keyManager.getPrivateKeyForPublicKey(key)).thenReturn(privateKey);

        when(naclFacade.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);

        final byte[] masterKey = "MASTER_KEY".getBytes();

        when(naclFacade.openAfterPrecomputation(RECIPIENT_BOX, RECIPIENT_NONCE, sharedKey))
                .thenReturn(masterKey);

        when(naclFacade.openAfterPrecomputation(CIPHER_TEXT, NONCE, new Key(masterKey)))
                .thenReturn("PLAINTEXT_MESSAGE".getBytes());

        final byte[] message = transactionService.retrieveUnencryptedTransaction(hash, key);

        assertThat(new String(message)).isEqualTo("PLAINTEXT_MESSAGE");

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(key);
        verify(naclFacade).computeSharedKey(senderKey, privateKey);
        verify(naclFacade, times(2)).openAfterPrecomputation(any(), any(), any());
        verify(payloadEncoder).decodePayloadWithRecipients(txnData);
        verify(keyManager).getPublicKeys();

    }

    @Test
    public void unencryptMessageUsesKeyAsRecipientKey() {

        final Key senderKey = new Key("SENDER_KEY".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList(RECIPIENT_BOX));
        when(encodedPayload.getRecipientNonce()).thenReturn(RECIPIENT_NONCE);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn(CIPHER_TEXT);
        when(encodedPayload.getCipherTextNonce()).thenReturn(NONCE);

        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});

        final Key key = new Key(new byte[]{11, 12, 13, 14});

        final Key privateKey = new Key("PRIVATE_KEY".getBytes());

        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        final byte[] txnData = UUID.randomUUID().toString().getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(hash, txnData);

        Key recipientKey = new Key("RECIPIENT_KEY".getBytes());

        final EncodedPayloadWithRecipients payloadWithRecipients = new EncodedPayloadWithRecipients(
            encodedPayload,
            singletonList(recipientKey)
        );

        when(payloadEncoder.decodePayloadWithRecipients(txnData))
                .thenReturn(payloadWithRecipients);

        when(dao.retrieveByHash(hash)).thenReturn(Optional.of(tx));

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(privateKey);

        when(naclFacade.computeSharedKey(recipientKey, privateKey)).thenReturn(sharedKey);

        doReturn(singleton(senderKey)).when(keyManager).getPublicKeys();

        final byte[] masterKey = "MASTER_KEY".getBytes();

        when(naclFacade.openAfterPrecomputation(RECIPIENT_BOX, RECIPIENT_NONCE, sharedKey))
                .thenReturn(masterKey);

        when(naclFacade.openAfterPrecomputation(CIPHER_TEXT, NONCE, new Key(masterKey)))
                .thenReturn("PLAINTEXT_MESSAGE".getBytes());

        final byte[] message = transactionService.retrieveUnencryptedTransaction(hash, key);

        assertThat(new String(message)).isEqualTo("PLAINTEXT_MESSAGE");

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(senderKey);
        verify(naclFacade).computeSharedKey(recipientKey, privateKey);
        verify(naclFacade, times(2)).openAfterPrecomputation(any(), any(), any());
        verify(payloadEncoder).decodePayloadWithRecipients(txnData);
        verify(keyManager).getPublicKeys();

    }


    //retrievePayload
    @Test
    public void missingTransactionThrowsError() {

        final MessageHash hash = new MessageHash(new byte[0]);

        doReturn(Optional.empty()).when(dao).retrieveByHash(any(MessageHash.class));

        final Throwable throwable = catchThrowable(() -> transactionService.retrievePayload(hash));

        assertThat(throwable)
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Message with hash " + hash + " was not found");

        verify(dao).retrieveByHash(hash);

    }


    @Test
    public void retrievePayload() {

        final EncryptedTransaction txn = mock(EncryptedTransaction.class);
        when(txn.getEncodedPayload()).thenReturn(new byte[0]);

        final MessageHash hash = new MessageHash(new byte[0]);

        when(dao.retrieveByHash(hash)).thenReturn(Optional.of(txn));

        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);

        when(payloadEncoder.decodePayloadWithRecipients(any())).thenReturn(encodedPayloadWithRecipients);

        EncodedPayloadWithRecipients result = transactionService.retrievePayload(hash);

        assertThat(encodedPayloadWithRecipients).isSameAs(result);

        verify(dao).retrieveByHash(hash);
        verify(payloadEncoder).decodePayloadWithRecipients(any());

    }

    @Test
    public void retrieveUnencryptedTransactionThrowsException() {
        when(naclFacade.openAfterPrecomputation(any(), any(), any()))
                .thenThrow(new RuntimeException("BANG"));

        final Key senderKey = new Key("SENDER_KEY".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList(RECIPIENT_BOX));
        when(encodedPayload.getRecipientNonce()).thenReturn(RECIPIENT_NONCE);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn(CIPHER_TEXT);
        when(encodedPayload.getCipherTextNonce()).thenReturn(NONCE);

        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});

        final Key key = new Key(new byte[]{11, 12, 13, 14});

        final Key privateKey = new Key("PRIVATE_KEY".getBytes());

        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        final byte[] txnData = UUID.randomUUID().toString().getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(hash, txnData);

        final EncodedPayloadWithRecipients payloadWithRecipients
            = new EncodedPayloadWithRecipients(encodedPayload, emptyList());

        when(payloadEncoder.decodePayloadWithRecipients(txnData))
                .thenReturn(payloadWithRecipients);

        when(dao.retrieveByHash(hash)).thenReturn(Optional.of(tx));

        when(keyManager.getPrivateKeyForPublicKey(key)).thenReturn(privateKey);

        when(naclFacade.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);

        final Throwable throwable = catchThrowable(() -> transactionService.retrieveUnencryptedTransaction(hash, key));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("BANG");

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(key);
        verify(naclFacade).computeSharedKey(senderKey, privateKey);
        verify(naclFacade).openAfterPrecomputation(any(), any(), any());
        verify(payloadEncoder).decodePayloadWithRecipients(txnData);
        verify(keyManager).getPublicKeys();
    }

}
