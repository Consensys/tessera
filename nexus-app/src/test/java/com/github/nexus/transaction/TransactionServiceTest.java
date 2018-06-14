package com.github.nexus.transaction;

import com.github.nexus.keys.KeyManager;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclException;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.Nonce;
import com.github.nexus.transaction.model.EncodedPayload;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;
import com.github.nexus.transaction.model.EncryptedTransaction;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private static final Key RECIPIENT_KEY = new Key(new byte[]{-1, -2, -3});

    private static final Key SENDER_KEY = new Key(new byte[]{1, 2, 3});
    private static final byte[] CIPHER_TEXT = new byte[]{4, 5, 6};
    private static final Nonce NONCE = new Nonce(new byte[]{7, 8, 9});
    private static final Nonce RECIPIENT_NONCE = new Nonce(new byte[]{10, 11, 12});
    private static final byte[] RECIPIENT_BOX = new byte[]{4, 5, 6};

    private final EncodedPayloadWithRecipients payload = new EncodedPayloadWithRecipients(
        new EncodedPayload(SENDER_KEY, CIPHER_TEXT, NONCE, singletonList(RECIPIENT_BOX), RECIPIENT_NONCE),
        singletonList(RECIPIENT_KEY)
    );

    private EncryptedTransaction encTx;

    private EncryptedTransactionDAO dao;

    private PayloadEncoder payloadEncoder;

    private KeyManager keyManager;

    private NaclFacade naclFacade;

    private TransactionService transactionService;

    @Before
    public void init() {
        this.dao = mock(EncryptedTransactionDAO.class);
        this.payloadEncoder = new PayloadEncoderImpl();
        this.keyManager = mock(KeyManager.class);
        this.naclFacade = mock(NaclFacade.class);

        this.encTx = new EncryptedTransaction(
            new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
            payloadEncoder.encode(payload)
        );

        this.transactionService = new TransactionServiceImpl(dao, payloadEncoder, keyManager, naclFacade);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(dao, keyManager, naclFacade);
    }

    @Test
    public void deleteDelegatesToDao() {

        transactionService.delete(new MessageHash(new byte[0]));

        verify(dao).delete(any());

    }

    @Test
    public void retrieveAllReturnsEmptyIfKeyIsNull() {

        doReturn(singletonList(encTx)).when(dao).retrieveAllTransactions();

        final Collection<EncodedPayloadWithRecipients> encodedPayloadWithRecipients
            = transactionService.retrieveAllForRecipient(null);

        assertThat(encodedPayloadWithRecipients).hasSize(0);
        verify(dao).retrieveAllTransactions();
    }

    @Test
    public void retrieveAllReturnsEmptyCollectionIfNoPayloadsExistForKey() {

        doReturn(singletonList(encTx)).when(dao).retrieveAllTransactions();

        final Collection<EncodedPayloadWithRecipients> encodedPayloadWithRecipients
            = transactionService.retrieveAllForRecipient(new Key(new byte[]{99}));

        assertThat(encodedPayloadWithRecipients).hasSize(0);
        verify(dao).retrieveAllTransactions();
    }

    @Test
    public void retrievePayloadThrowsExceptionIfMessageDoesntExist() {
        final MessageHash nonexistantHash = new MessageHash(new byte[]{1});

        doReturn(Optional.empty()).when(dao).retrieveByHash(nonexistantHash);

        final Throwable throwable
            = catchThrowable(() -> transactionService.retrievePayload(nonexistantHash, RECIPIENT_KEY));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Message with hash " + nonexistantHash + " was not found");

        verify(dao).retrieveByHash(nonexistantHash);

    }

    @Test
    public void exceptionThrownWhenRecipientNotPartyToTransaction() {

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Key unintendedRecipient = new Key(new byte[]{11, 12, 13, 14});

        doReturn(Optional.of(encTx)).when(dao).retrieveByHash(hash);

        final Throwable throwable = catchThrowable(() -> transactionService.retrievePayload(hash, unintendedRecipient));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Recipient " + unintendedRecipient + " is not a recipient of transaction " + hash);

        verify(dao).retrieveByHash(hash);

    }

    @Test
    public void encodedTransactionReturnedWhenTransactionFoundAndVerified() {

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        doReturn(Optional.of(encTx)).when(dao).retrieveByHash(hash);

        final EncodedPayload encodedPayload = transactionService.retrievePayload(hash, RECIPIENT_KEY);

        assertThat(encodedPayload).isEqualToComparingFieldByFieldRecursively(payload.getEncodedPayload());

        verify(dao).retrieveByHash(hash);

    }

    @Test
    public void encodedTransactionReturnedWhenTransactionFoundAndVerifiedWithOnlyIntendedRecipient() {

        final Key secondRecipient = new Key(new byte[]{21, 22, 23, 24});
        final byte[] secondSealedbox = new byte[]{1, 12, 23, 34, 45};

        final EncodedPayloadWithRecipients payloadWithTwoRecs = new EncodedPayloadWithRecipients(
            new EncodedPayload(SENDER_KEY, CIPHER_TEXT, NONCE, asList(RECIPIENT_BOX, secondSealedbox), RECIPIENT_NONCE),
            asList(RECIPIENT_KEY, secondRecipient)
        );

        final EncryptedTransaction encTxTwoRecs = new EncryptedTransaction(
            new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
            payloadEncoder.encode(payloadWithTwoRecs)
        );

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        doReturn(Optional.of(encTxTwoRecs)).when(dao).retrieveByHash(hash);

        final EncodedPayload encodedPayload = transactionService.retrievePayload(hash, RECIPIENT_KEY);

        assertThat(encodedPayload).isEqualToComparingFieldByFieldRecursively(payload.getEncodedPayload());

        verify(dao).retrieveByHash(hash);

    }

    @Test
    public void storingPayloadCalculatesSha3512Hash() {

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] digest = digestSHA3.digest(payload.getEncodedPayload().getCipherText());

        final MessageHash hash = transactionService.storeEncodedPayload(payload);

        assertThat(hash).isEqualTo(new MessageHash(digest));
        verify(dao).save(any(EncryptedTransaction.class));

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
        assertThat(encodedPayload.getCipherTextNonce()).isEqualTo(new Nonce(new byte[]{1}));
        assertThat(encodedPayload.getRecipientBoxes()).hasSize(1);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly("CIPHER_MASTER".getBytes());
        assertThat(encodedPayload.getRecipientNonce()).isEqualTo(new Nonce(new byte[]{2}));

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

        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});
        final Key key = new Key(new byte[]{11, 12, 13, 14});
        final Key privateKey = new Key("PRIVATE_KEY".getBytes());
        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        final EncodedPayloadWithRecipients payloadWithRecipients = new EncodedPayloadWithRecipients(
            payload.getEncodedPayload(),
            emptyList()
        );

        final EncryptedTransaction tx
            = new EncryptedTransaction(hash.getHashBytes(), payloadEncoder.encode(payloadWithRecipients));

        doReturn(Optional.of(tx)).when(dao).retrieveByHash(hash);

        doReturn(privateKey).when(keyManager).getPrivateKeyForPublicKey(key);
        doReturn(sharedKey).when(naclFacade).computeSharedKey(payload.getEncodedPayload().getSenderKey(), privateKey);

        doReturn("PLAINTEXT_KEY".getBytes())
            .when(naclFacade)
            .openAfterPrecomputation(RECIPIENT_BOX, RECIPIENT_NONCE, sharedKey);
        doReturn("PLAINTEXT_MESSAGE".getBytes())
            .when(naclFacade)
            .openAfterPrecomputation(CIPHER_TEXT, NONCE, new Key("PLAINTEXT_KEY".getBytes()));

        final byte[] message = transactionService.retrieveUnencryptedTransaction(hash, key);

        assertThat(new String(message)).isEqualTo("PLAINTEXT_MESSAGE");

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(key);
        verify(naclFacade).computeSharedKey(payload.getEncodedPayload().getSenderKey(), privateKey);
        verify(naclFacade, times(2)).openAfterPrecomputation(any(), any(), any());

    }

    @Test
    public void unencryptMessageUsesKeyAsRecipientKey() {

        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});
        final Key key = new Key(new byte[]{11, 12, 13, 14});
        final Key privateKey = new Key("PRIVATE_KEY".getBytes());
        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        doReturn(Optional.of(encTx)).when(dao).retrieveByHash(hash);

        doReturn(privateKey).when(keyManager).getPrivateKeyForPublicKey(payload.getEncodedPayload().getSenderKey());
        doReturn(sharedKey).when(naclFacade).computeSharedKey(payload.getRecipientKeys().get(0), privateKey);

        doReturn("PLAINTEXT_KEY".getBytes())
            .when(naclFacade)
            .openAfterPrecomputation(RECIPIENT_BOX, RECIPIENT_NONCE, sharedKey);
        doReturn("PLAINTEXT_MESSAGE".getBytes())
            .when(naclFacade)
            .openAfterPrecomputation(CIPHER_TEXT, NONCE, new Key("PLAINTEXT_KEY".getBytes()));

        final byte[] message = transactionService.retrieveUnencryptedTransaction(hash, key);

        assertThat(new String(message)).isEqualTo("PLAINTEXT_MESSAGE");

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(payload.getEncodedPayload().getSenderKey());
        verify(naclFacade).computeSharedKey(payload.getRecipientKeys().get(0), privateKey);
        verify(naclFacade, times(2)).openAfterPrecomputation(any(), any(), any());

    }

    @Test
    public void exceptionThrownIfDecryptionFails() {
        final MessageHash hash = new MessageHash(new byte[]{78, 87, 45, 54});
        final Key key = new Key(new byte[]{11, 12, 13, 14});
        final Key privateKey = new Key("PRIVATE_KEY".getBytes());
        final Key sharedKey = new Key(new byte[]{111, 12, 13, 14});

        doReturn(Optional.of(encTx)).when(dao).retrieveByHash(hash);

        doReturn(privateKey).when(keyManager).getPrivateKeyForPublicKey(payload.getEncodedPayload().getSenderKey());
        doReturn(sharedKey).when(naclFacade).computeSharedKey(payload.getRecipientKeys().get(0), privateKey);

        doThrow(NaclException.class)
            .when(naclFacade)
            .openAfterPrecomputation(RECIPIENT_BOX, RECIPIENT_NONCE, sharedKey);

        final Throwable throwable = catchThrowable(() -> transactionService.retrieveUnencryptedTransaction(hash, key));

        assertThat(throwable).isInstanceOf(NaclException.class);

        verify(dao).retrieveByHash(hash);
        verify(keyManager).getPrivateKeyForPublicKey(payload.getEncodedPayload().getSenderKey());
        verify(naclFacade).computeSharedKey(payload.getRecipientKeys().get(0), privateKey);
        verify(naclFacade).openAfterPrecomputation(any(), any(), any());
    }

}
