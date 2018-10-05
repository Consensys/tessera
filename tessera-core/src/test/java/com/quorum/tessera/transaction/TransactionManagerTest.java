package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import com.quorum.tessera.util.Base64Decoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private TransactionManager transactionManager;

    private KeyManager keyManager;

    private NaclFacade naclFacade;

    private EncryptedTransactionDAO encryptedTransactionDAO;


    private PayloadEncoder payloadEncoder;

    private PayloadPublisher payloadPublisher;
    
    @Before
    public void onSetUp() {

        keyManager = mock(KeyManager.class);

        naclFacade = mock(NaclFacade.class);

        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);


        payloadPublisher = mock(PayloadPublisher.class);

        payloadEncoder = mock(PayloadEncoder.class);

        transactionManager = new TransactionManagerImpl(Base64Decoder.create(), payloadEncoder,
                keyManager, naclFacade, encryptedTransactionDAO,payloadPublisher);
    }

    @After
    public void onTearDown() {

        verifyNoMoreInteractions(keyManager, naclFacade, encryptedTransactionDAO, payloadPublisher, payloadEncoder);
    }

    @Test
    public void delete() {

        byte[] data = "BOGUSKEY".getBytes();

        DeleteRequest deleteRequest = new DeleteRequest();
        String key = Base64.getEncoder().encodeToString(data);
        deleteRequest.setKey(key);

        List<MessageHash> results = new ArrayList<>();

        doAnswer((invocation) -> {
            results.add(invocation.getArgument(0));
            return null;
        }).when(encryptedTransactionDAO).delete(any(MessageHash.class));

        transactionManager.delete(deleteRequest);

        assertThat(results).hasSize(1);

        MessageHash result = results.get(0);

        assertThat(result.getHashBytes()).isEqualTo(data);

        verify(encryptedTransactionDAO).delete(any(MessageHash.class));
    }


    //@Test
    public void send() {

        when(keyManager.defaultPublicKey()).thenReturn(new Key("".getBytes()));

        String senderPublicKey = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String payload = Base64.getEncoder().encodeToString("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(senderPublicKey);
        sendRequest.setPayload(payload);
        sendRequest.setTo(new String[]{"RECIPIENT"});

        SendResponse response = transactionManager.send(sendRequest);

        verify(keyManager).defaultPublicKey();
        verify(keyManager).getForwardingKeys();

    }

    @Test
    public void storePayload() {

        byte[] data = Base64.getEncoder().encode("PAYLOAD".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        EncodedPayloadWithRecipients encodedPayloadWithRecipients
                = new EncodedPayloadWithRecipients(encodedPayload, Collections.EMPTY_LIST);

        when(payloadEncoder.decodePayloadWithRecipients(data)).thenReturn(encodedPayloadWithRecipients);

        transactionManager.storePayload(data);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).encode(encodedPayloadWithRecipients);
        verify(payloadEncoder).decodePayloadWithRecipients(data);
    }

    @Test
    public void receiveNoRecipeientsKeyManagerHasSenderKey() {

        final Key senderPublicKey = mock(Key.class);
        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderPublicKey));

        final Key senderPrivateKey = mock(Key.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        when(encryptedTransaction.getEncodedPayload()).thenReturn("".getBytes());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        //recipientBox = encodedPayload.getRecipientBoxes()
        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderPublicKey);

        final byte[] recipientBox = "BOX".getBytes();
        when(encodedPayload.getRecipientBoxes()).thenReturn(Arrays.asList(recipientBox));

        final Nonce recipientNonce = mock(Nonce.class);
        when(encodedPayload.getRecipientNonce()).thenReturn(recipientNonce);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);

        final Key recipienPublicKey = mock(Key.class);

        when(encodedPayloadWithRecipients.getRecipientKeys()).thenReturn(Arrays.asList(recipienPublicKey));
        

        when(payloadEncoder.decodePayloadWithRecipients("".getBytes())).thenReturn(encodedPayloadWithRecipients);

        final Key sharedKey = mock(Key.class);

        final byte[] masterKeyBytes = "masterKeyBytes".getBytes();

        when(naclFacade.openAfterPrecomputation(recipientBox, recipientNonce, sharedKey)).thenReturn(masterKeyBytes);

        when(naclFacade.computeSharedKey(recipienPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);
        when(encodedPayload.getCipherText()).thenReturn(cipherText);
        when(encodedPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);

        when(naclFacade.openAfterPrecomputation(cipherText, cipherTextNonce, new Key(masterKeyBytes))).thenReturn("RESULT".getBytes());

        final byte[] data = "BOGUSKEY".getBytes();
        String key = Base64.getEncoder().encodeToString(data);

        final ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);

        ReceiveResponse response = transactionManager.receive(receiveRequest);

        assertThat(response).isNotNull();
        
       
        assertThat(response.getPayload()).isEqualTo(Base64.getEncoder().encodeToString("RESULT".getBytes()));

        verify(keyManager, times(2)).getPublicKeys();
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients("".getBytes());
        verify(naclFacade).openAfterPrecomputation(recipientBox, recipientNonce, sharedKey);
        verify(naclFacade).computeSharedKey(recipienPublicKey, senderPrivateKey);
        verify(naclFacade).openAfterPrecomputation(cipherText, cipherTextNonce, new Key(masterKeyBytes));

    }
    
    

}
