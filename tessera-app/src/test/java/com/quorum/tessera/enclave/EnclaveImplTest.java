package com.quorum.tessera.enclave;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.transaction.PayloadEncoder;
import com.quorum.tessera.transaction.TransactionService;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    private static final Key EMPTY_KEY = new Key(new byte[0]);

    private static final Key RECIPIENT_KEY = new Key(new byte[]{-1, -2, -3});
    private static final Key SENDER_KEY = new Key(new byte[]{1, 2, 3});
    private static final byte[] CIPHER_TEXT = new byte[]{4, 5, 6};
    private static final Nonce NONCE = new Nonce(new byte[]{7, 8, 9});
    private static final Nonce RECIPIENT_NONCE = new Nonce(new byte[]{10, 11, 12});
    private static final byte[] RECIPIENT_BOX = new byte[]{4, 5, 6};

    private static final Key FORWARDING_KEY = new Key("forwarding".getBytes());

    private final EncodedPayloadWithRecipients payload = new EncodedPayloadWithRecipients(
        new EncodedPayload(SENDER_KEY, CIPHER_TEXT, NONCE, singletonList(RECIPIENT_BOX), RECIPIENT_NONCE),
        singletonList(RECIPIENT_KEY)
    );

    private TransactionService transactionService;

    private PartyInfoService partyInfoService;

    private PayloadEncoder encoder;

    private P2pClient p2pClient;

    private KeyManager keyManager;

    private EnclaveImpl enclave;

    @Before
    public void setUp() {
        this.transactionService = mock(TransactionService.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.encoder = mock(PayloadEncoder.class);
        this.p2pClient = mock(P2pClient.class);
        this.keyManager = mock(KeyManager.class);

        enclave = new EnclaveImpl(transactionService, partyInfoService, encoder, keyManager, p2pClient);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(transactionService, partyInfoService, encoder, p2pClient, keyManager);
    }

    @Test
    public void testDelete() {

        enclave.delete(new byte[0]);
        verify(transactionService).delete(any(MessageHash.class));
    }

    @Test
    public void receiveWhenKeyProvided() {
        final byte[] hash = new byte[0];

        enclave.receive(hash, Optional.of(new byte[0]));
        
        verify(transactionService).retrieveUnencryptedTransaction(eq(new MessageHash(hash)), eq(EMPTY_KEY));
    }

    @Test
    public void receiveWhenNoKeyProvidedAndOneMatch() {
        final byte[] hash = new byte[0];

        when(keyManager.getPublicKeys()).thenReturn(singleton(EMPTY_KEY));
        when(transactionService.retrieveUnencryptedTransaction(any(MessageHash.class), any(Key.class)))
            .thenReturn(new byte[]{1, 2, 3});

        final byte[] received = enclave.receive(hash, Optional.empty());

        assertThat(received).containsExactly(1, 2, 3);

        verify(keyManager).getPublicKeys();
        verify(transactionService).retrieveUnencryptedTransaction(eq(new MessageHash(hash)), any(Key.class));
    }

    @Test
    public void receiveWhenNoKeyProvidedAndNoneMatch() {
        final byte[] hash = new byte[0];

        when(keyManager.getPublicKeys()).thenReturn(singleton(EMPTY_KEY));
        when(transactionService.retrieveUnencryptedTransaction(any(MessageHash.class), any(Key.class))).thenThrow(NaclException.class);

        final Throwable throwable = catchThrowable(() -> enclave.receive(hash, Optional.empty()));
        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("No key found that could decrypt the requested payload: ");

        verify(keyManager).getPublicKeys();
        verify(transactionService).retrieveUnencryptedTransaction(eq(new MessageHash(hash)), any(Key.class));
    }

    @Test
    public void storeDoesntNeedToFetchSenderKeyIfOneProvided() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        enclave.store(Optional.of(new byte[0]), new byte[0][0], new byte[0]);

        verify(transactionService).encryptPayload(any(), any(), any());
        verify(transactionService).storeEncodedPayload(payload);
        verify(keyManager).getForwardingKeys();
        verify(keyManager, never()).defaultPublicKey();
    }

    @Test
    public void storeFetchesDefaultKeyIfOneNotProvided() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        doReturn(SENDER_KEY).when(keyManager).defaultPublicKey();

        enclave.store(Optional.empty(), new byte[0][0], new byte[0]);

        verify(transactionService).encryptPayload(any(), any(), any());
        verify(transactionService).storeEncodedPayload(payload);
        verify(keyManager).defaultPublicKey();
        verify(keyManager).getForwardingKeys();
    }

    @Test
    public void testStorePayload() {
        when(transactionService.storeEncodedPayload(any(EncodedPayloadWithRecipients.class)))
            .thenReturn(new MessageHash(new byte[0]));
        enclave.storePayload(new byte[0]);
        verify(encoder).decodePayloadWithRecipients(any());
        verify(transactionService).storeEncodedPayload(any());
    }

    //send in a transaction with 0 recipients, 1 should get added from the forward list
    @Test
    public void storeAddsForwardingList() {

        final EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        doReturn(payload).when(transactionService).encryptPayload(any(), any(), any());

        doReturn(singleton(FORWARDING_KEY)).when(keyManager).getForwardingKeys();
        doReturn(SENDER_KEY).when(keyManager).defaultPublicKey();
        doReturn("testurl.com").when(partyInfoService).getURLFromRecipientKey(any(Key.class));
        doReturn(new PartyInfo("testurl.com", emptySet(), emptySet())).when(partyInfoService).getPartyInfo();

        enclave.store(Optional.empty(), new byte[0][0], new byte[0]);

        final ArgumentCaptor<List<Key>> captor = ArgumentCaptor.forClass(List.class);

        verify(transactionService).encryptPayload(any(), any(), captor.capture());
        verify(transactionService).storeEncodedPayload(payload);
        verify(keyManager).defaultPublicKey();
        verify(keyManager).getForwardingKeys();
        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).getURLFromRecipientKey(FORWARDING_KEY);

        assertThat(captor.getValue()).hasSize(1).containsExactlyInAnyOrder(FORWARDING_KEY);
    }

    @Test
    public void testStoreWithRecipientAndPublish() {

        final EncodedPayload encodedPayload = new EncodedPayload(
            EMPTY_KEY,
            new byte[0],
            new Nonce(new byte[0]),
            Arrays.asList("box1".getBytes(), "box2".getBytes()),
            new Nonce(new byte[0])
        );

        final List<Key> recipientKeys = Arrays.asList(new Key("somekey".getBytes()), new Key("key2".getBytes()));

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        final Key recipientKey = new Key("somekey".getBytes());

        final PartyInfo partyInfo = new PartyInfo("ownurl.com", emptySet(), emptySet());

        doReturn(encodedPayloadWithRecipients)
            .when(transactionService)
            .encryptPayload(any(), any(), any());

        doReturn(new MessageHash("somehash".getBytes()))
            .when(transactionService)
            .storeEncodedPayload(encodedPayloadWithRecipients);

        doReturn("someurl.com").when(partyInfoService).getURLFromRecipientKey(recipientKey);

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();

        enclave.store(Optional.of(new byte[0]), new byte[][]{"somekey".getBytes(), "key2".getBytes()}, new byte[0]);

        enclave.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(transactionService).encryptPayload(any(), any(), any());
        verify(transactionService).storeEncodedPayload(encodedPayloadWithRecipients);

        verify(encoder, times(3)).encode(any(EncodedPayloadWithRecipients.class));

        verify(p2pClient, times(3)).push(any(), any());

        verify(partyInfoService, times(2)).getURLFromRecipientKey(recipientKey);
        verify(partyInfoService).getURLFromRecipientKey(new Key("key2".getBytes()));
        verify(partyInfoService, times(3)).getPartyInfo();
        verify(partyInfoService, times(3)).getPartyInfo();

        verify(keyManager).getForwardingKeys();
    }

    @Test
    public void testResendAll() {
        EncodedPayload encodedPayload =
            new EncodedPayload(EMPTY_KEY,
                new byte[0],
                new Nonce(new byte[0]),
                Arrays.asList("box1".getBytes(), "box2".getBytes()),
                new Nonce(new byte[0]));
        List<Key> recipientKeys = Arrays.asList(new Key("somekey".getBytes()), new Key("key2".getBytes()));
        EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        when(transactionService.retrieveAllForRecipient(any()))
            .thenReturn(singletonList(encodedPayloadWithRecipients));

        Key recipientKey = new Key("somekey".getBytes());
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn("http://someurl.com");
        PartyInfo partyInfo = new PartyInfo("http://someurl.com", emptySet(), emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        enclave.resendAll("someKey".getBytes());

        verify(transactionService).retrieveAllForRecipient(any());

        verify(encoder).encode(any(EncodedPayloadWithRecipients.class));

        verify(p2pClient).push(any(), any());

        verify(partyInfoService, times(2)).getURLFromRecipientKey(any());
        verify(partyInfoService, times(2)).getPartyInfo();

    }

    @Test
    public void retrievePayloadThrowsExceptionIfMessageDoesntExist() {
        final MessageHash nonexistantHash = new MessageHash(new byte[]{1});

        final TransactionNotFoundException exception
            = new TransactionNotFoundException("Message with hash " + nonexistantHash + " was not found");

        doThrow(exception).when(transactionService).retrievePayload(nonexistantHash);

        final Throwable throwable
            = catchThrowable(() -> enclave.fetchTransactionForRecipient(nonexistantHash, RECIPIENT_KEY));

        assertThat(throwable)
            .isInstanceOf(TransactionNotFoundException.class)
            .hasMessage("Message with hash " + nonexistantHash + " was not found");

        verify(transactionService).retrievePayload(nonexistantHash);

    }

    @Test
    public void exceptionThrownWhenRecipientNotPartyToTransaction() {

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Key unintendedRecipient = new Key(new byte[]{11, 12, 13, 14});

        doReturn(payload).when(transactionService).retrievePayload(hash);

        final Throwable throwable
            = catchThrowable(() -> enclave.fetchTransactionForRecipient(hash, unintendedRecipient));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Recipient " + unintendedRecipient + " is not a recipient of transaction " + hash);

        verify(transactionService).retrievePayload(hash);

    }

    @Test
    public void encodedTransactionReturnedWhenTransactionFoundAndVerified() {

        final Key secondRecipient = new Key(new byte[]{21, 22, 23, 24});
        final byte[] secondSealedbox = new byte[]{1, 12, 23, 34, 45};

        final EncodedPayloadWithRecipients payloadWithTwoRecs = new EncodedPayloadWithRecipients(
            new EncodedPayload(SENDER_KEY, CIPHER_TEXT, NONCE, asList(RECIPIENT_BOX, secondSealedbox), RECIPIENT_NONCE),
            asList(RECIPIENT_KEY, secondRecipient)
        );

        final MessageHash hash = new MessageHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        doReturn(payloadWithTwoRecs).when(transactionService).retrievePayload(hash);

        final EncodedPayloadWithRecipients encodedPayload = enclave.fetchTransactionForRecipient(hash, RECIPIENT_KEY);

        assertThat(encodedPayload.getEncodedPayload()).isEqualToComparingFieldByFieldRecursively(payload.getEncodedPayload());
        assertThat(encodedPayload.getRecipientKeys()).isEmpty();

        verify(transactionService).retrievePayload(hash);

    }

}
