package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.key.KeyManager;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.Nonce;
import com.github.nexus.node.PartyInfoService;
import com.github.nexus.node.PostDelegate;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.transaction.PayloadEncoder;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.model.EncodedPayload;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    private TransactionService transactionService;

    private PartyInfoService partyInfoService;

    private PayloadEncoder encoder;

    private PostDelegate postDelegate;

    private KeyManager keyManager;

    private EnclaveImpl enclave;

    @Before
    public void setUp() {
        this.transactionService = mock(TransactionService.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.encoder = mock(PayloadEncoder.class);
        this.postDelegate = mock(PostDelegate.class);
        this.keyManager = mock(KeyManager.class);

        enclave = new EnclaveImpl(transactionService, partyInfoService, encoder, postDelegate, keyManager);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(transactionService, partyInfoService, encoder, postDelegate, keyManager);
    }

    @Test
    public void testDelete() {
   
        enclave.delete(new byte[0]);
        verify(transactionService).delete(any(MessageHash.class));
    }

    @Test
    public void receiveWhenKeyProvided() {
        final byte[] hash = new byte[]{};
        final byte[] key = new byte[]{};

        enclave.receive(hash, Optional.of(new byte[0]));

        verify(transactionService).retrieveUnencryptedTransaction(eq(new MessageHash(hash)), eq(new Key(key)));
    }

    @Test
    public void receiveWhenNoKeyProvided() {
        final byte[] hash = new byte[]{};

        doReturn(new Key(new byte[]{})).when(keyManager).defaultPublicKey();

        enclave.receive(hash, Optional.empty());

        verify(keyManager).defaultPublicKey();
        verify(transactionService).retrieveUnencryptedTransaction(eq(new MessageHash(hash)), any(Key.class));
    }

    @Test
    public void storeDoesntNeedToFetchSenderKeyIfOneProvided() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        enclave.store(Optional.of(new byte[0]), new byte[0][0], new byte[0]);

        verify(transactionService).encryptPayload(any(), any(), any());

        verify(transactionService).storeEncodedPayload(payload);

        verifyZeroInteractions(keyManager);
    }

    @Test
    public void storeFetchesDefaultKeyIfOneNotProvided() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        doReturn(new Key(new byte[0])).when(keyManager).defaultPublicKey();

        enclave.store(Optional.empty(), new byte[0][0], new byte[0]);


        verify(transactionService).encryptPayload(any(), any(), any());
        verify(transactionService).storeEncodedPayload(payload);
        verify(keyManager).defaultPublicKey();
    }

    @Test
    public void testStorePayload() {
        when(transactionService.storeEncodedPayload(any(EncodedPayloadWithRecipients.class)))
            .thenReturn(new MessageHash(new byte[0]));
        enclave.storePayload(new byte[0]);
        verify(encoder).decodePayloadWithRecipients(any());
        verify(transactionService).storeEncodedPayload(any());
    }

    @Ignore
    public void testStoreWithRecipientStuff() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        when(partyInfoService.getURLFromRecipientKey(any())).thenReturn("someurl.com");
        PartyInfo partyInfo = new PartyInfo("ownurl.com", emptySet(), emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        byte[][] recipients = new byte[1][1];
        recipients[0] = new byte[]{'P'};

        enclave.store(Optional.of(new byte[0]), recipients, new byte[0]);

        verify(transactionService).encryptPayload(any(), any(), any());

        verify(transactionService).storeEncodedPayload(payload);
    }

    @Test
    public void testStoreWithRecipientAndPublish() {

        final EncodedPayload encodedPayload = new EncodedPayload(
            new Key(new byte[0]),
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

        verify(postDelegate, times(3)).doPost(any(), any(), any());

        verify(partyInfoService, times(2)).getURLFromRecipientKey(recipientKey);
        verify(partyInfoService).getURLFromRecipientKey(new Key("key2".getBytes()));
        verify(partyInfoService, times(3)).getPartyInfo();
        verify(partyInfoService, times(3)).getPartyInfo();
    }

    @Test
    public void testResendAll() {
        EncodedPayload encodedPayload =
            new EncodedPayload(new Key(new byte[0]),
                new byte[0],
                new Nonce(new byte[0]),
                Arrays.asList("box1".getBytes(), "box2".getBytes()),
                new Nonce(new byte[0]));
        List<Key> recipientKeys = Arrays.asList(new Key("somekey".getBytes()), new Key("key2".getBytes()));
        EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        when(transactionService.retrieveAllForRecipient(any()))
            .thenReturn(Arrays.asList(encodedPayloadWithRecipients));

        Key recipientKey = new Key("somekey".getBytes());
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn("http://someurl.com");
        PartyInfo partyInfo = new PartyInfo("http://someurl.com", emptySet(), emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        enclave.resendAll("someKey".getBytes());

        verify(transactionService).retrieveAllForRecipient(any());

        verify(encoder).encode(any(EncodedPayloadWithRecipients.class));

        verify(postDelegate).doPost(any(), any(), any());

        verify(partyInfoService, times(2)).getURLFromRecipientKey(any());
        verify(partyInfoService, times(2)).getPartyInfo();

    }
}
