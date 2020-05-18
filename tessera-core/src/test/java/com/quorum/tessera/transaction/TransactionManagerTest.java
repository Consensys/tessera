package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static java.util.Collections.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private PartyInfoService partyInfoService;

    private ResendManager resendManager;

    private Enclave enclave;

    private MessageHashFactory messageHashFactory = MessageHashFactory.create();

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);

        when(enclave.findInvalidSecurityHashes(any(), any())).thenReturn(emptySet());

        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        partyInfoService = mock(PartyInfoService.class);
        this.resendManager = mock(ResendManager.class);

        transactionManager =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        partyInfoService,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        1000);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, encryptedTransactionDAO, partyInfoService, enclave);
    }

    @Test
    public void send() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getCipherTextNonce()).thenReturn(new Nonce("NONCE".getBytes()));
        when(encodedPayload.getRecipientNonce()).thenReturn(new Nonce("NONCE".getBytes()));
        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder, times(2)).forRecipient(eq(encodedPayload), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendWithDuplicateRecipients() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getCipherTextNonce()).thenReturn(new Nonce("NONCE".getBytes()));
        when(encodedPayload.getRecipientNonce()).thenReturn(new Nonce("NONCE".getBytes()));
        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));

        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder, times(2)).forRecipient(eq(encodedPayload), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Ignore
    @Test
    public void sendWithAffectedContractTransactionsNotFound() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});
        sendRequest.setExecHash("execHash");

        assertThatExceptionOfType(PrivacyViolationException.class)
                .isThrownBy(
                        () -> {
                            transactionManager.send(sendRequest);
                            failBecauseExceptionWasNotThrown(Exception.class);
                        })
                .withMessageContaining("Unable to find affectedContractTransaction")
                .withMessageContaining(affectedTxHash);

        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(enclave).getForwardingKeys();
    }

    @Ignore
    @Test
    public void sendWithPrivateStateConsensusMismatched() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});

        EncryptedTransaction mockAffectedTransaction = mock(EncryptedTransaction.class);
        EncodedPayload mockAffectedEncodedPayload = mock(EncodedPayload.class);
        when(encryptedTransactionDAO.retrieveByHash(any())).thenReturn(Optional.of(mockAffectedTransaction));
        when(mockAffectedTransaction.getEncodedPayload()).thenReturn("payload".getBytes());
        when(payloadEncoder.decode("payload".getBytes())).thenReturn(mockAffectedEncodedPayload);
        when(mockAffectedEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        assertThatExceptionOfType(PrivacyViolationException.class)
                .describedAs("Psv flag mismatched")
                .isThrownBy(
                        () -> {
                            transactionManager.send(sendRequest);
                            failBecauseExceptionWasNotThrown(any());
                        })
                .withMessageContaining("Private state validation flag mismatched with Affected Txn")
                .withMessageContaining(affectedTxHash);

        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(payloadEncoder).decode("payload".getBytes());
        verify(enclave).getForwardingKeys();
    }

    @Ignore
    @Test
    public void sendWithAffectedContractNoPsv() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setPrivacyFlag(1);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});

        EncryptedTransaction mockAffectedTransaction = mock(EncryptedTransaction.class);
        EncodedPayload mockAffectedEncodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.findByHashes(anyCollection())).thenReturn(List.of(mockAffectedTransaction));
        when(mockAffectedTransaction.getEncodedPayload()).thenReturn("payload".getBytes());
        when(mockAffectedEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
     //   when(payloadEncoder.decode("payload".getBytes())).thenReturn(mockAffectedEncodedPayload);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder, times(2)).forRecipient(eq(encodedPayload), any(PublicKey.class));
        verify(encryptedTransactionDAO).findByHashes(anyCollection());
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Ignore
    @Test
    public void sendWithAffectedContractsAndRecipientsMismatched() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(enclave.encryptPayload(any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setPrivacyFlag(3);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});
        sendRequest.setExecHash("execHash");

        EncryptedTransaction mockAffectedTransaction = mock(EncryptedTransaction.class);
        EncodedPayload mockAffectedEncodedPayload = mock(EncodedPayload.class);
        when(encryptedTransactionDAO.retrieveByHash(any())).thenReturn(Optional.of(mockAffectedTransaction));
        when(mockAffectedTransaction.getEncodedPayload()).thenReturn("payload".getBytes());
        when(payloadEncoder.decode("payload".getBytes())).thenReturn(mockAffectedEncodedPayload);
        when(mockAffectedEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        assertThatExceptionOfType(PrivacyViolationException.class)
                .describedAs("Recipients mismatched")
                .isThrownBy(
                        () -> {
                            transactionManager.send(sendRequest);
                            failBecauseExceptionWasNotThrown(any());
                        })
                .withMessageContaining("Recipients mismatched for Affected Txn");

        verify(payloadEncoder).decode("payload".getBytes());
        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(enclave).getForwardingKeys();
    }

    @Ignore
    @Test
    public void sendWithAffectedContractAndItHasMoreRecipients() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        final String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        final String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        List<PublicKey> recipientList = new ArrayList<>();
        recipientList.add(PublicKey.from("SENDER".getBytes()));
        recipientList.add(PublicKey.from("RECEIVER".getBytes()));
        recipientList.add(PublicKey.from("SOME-OTHER-PARTICIPANT".getBytes()));

        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});
        sendRequest.setPrivacyFlag(3);

        EncryptedTransaction mockAffectedTransaction = mock(EncryptedTransaction.class);
        EncodedPayload mockAffectedEncodedPayload = mock(EncodedPayload.class);
        when(encryptedTransactionDAO.retrieveByHash(any())).thenReturn(Optional.of(mockAffectedTransaction));
        when(mockAffectedTransaction.getEncodedPayload()).thenReturn("payload".getBytes());
        when(payloadEncoder.decode("payload".getBytes())).thenReturn(mockAffectedEncodedPayload);
        when(mockAffectedEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(mockAffectedEncodedPayload.getRecipientKeys()).thenReturn(recipientList);

        assertThatExceptionOfType(PrivacyViolationException.class)
                .describedAs("Recipients mismatched")
                .isThrownBy(
                        () -> {
                            transactionManager.send(sendRequest);
                            failBecauseExceptionWasNotThrown(any());
                        })
                .withMessageContaining("Recipients mismatched for Affected Txn");

        verify(payloadEncoder).decode("payload".getBytes());
        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(enclave).getForwardingKeys();
    }

    @Ignore
    @Test
    public void sendWithAffectedContractsAndPsvCheck() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        final String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        final String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        List<PublicKey> recipientList = new ArrayList<>();
        recipientList.add(PublicKey.from("SENDER".getBytes()));
        recipientList.add(PublicKey.from("RECEIVER".getBytes()));

        final String affectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        sendRequest.setPrivacyFlag(3);
        sendRequest.setAffectedContractTransactions(new String[] {affectedTxHash});
        sendRequest.setExecHash("execHash");

        EncryptedTransaction mockAffectedTransaction = mock(EncryptedTransaction.class);
        MessageHash messageHash = new MessageHash();
        messageHash.setHashBytes(Base64.getDecoder().decode(affectedTxHash));

        EncodedPayload mockAffectedEncodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.findByHashes(anyCollection())).thenReturn(List.of(mockAffectedTransaction));
        when(mockAffectedTransaction.getHash()).thenReturn(messageHash);

        byte[] payloadBytes = "payload".getBytes();

        when(mockAffectedTransaction.getEncodedPayload()).thenReturn(payloadBytes);
        when(payloadEncoder.decode(payloadBytes)).thenReturn(mockAffectedEncodedPayload);
        when(mockAffectedEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(mockAffectedEncodedPayload.getRecipientKeys()).thenReturn(recipientList);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder).decode(payloadBytes);
        verify(payloadEncoder, times(2)).forRecipient(eq(encodedPayload), any(PublicKey.class));
        verify(encryptedTransactionDAO).findByHashes(anyCollection());
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendRecipientKeyNotKnownToNode() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        final Set<PublicKey> recipients =
                Stream.of(Stream.of(receiver).map(Base64.getDecoder()::decode).toArray(byte[][]::new))
                        .map(PublicKey::from)
                        .collect(Collectors.toSet());

        when(enclave.getPublicKeys()).thenReturn(recipients);

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder).forRecipient(eq(encodedPayload), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Ignore
    @Test
    public void sendSignedTransaction() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        new MessageHash("HASH".getBytes()),
                        "ENCRYPTED_PAYLOAD".getBytes(),
                        "ENCRYPTED_KEY".getBytes(),
                        "NONCE".getBytes(),
                        "SENDER".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedRawTransaction));

        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any(), any(), any(), any())).thenReturn(payload);

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());
        sendSignedRequest.setAffectedContractTransactions(new String[] {});
        sendSignedRequest.setPrivacyFlag(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any(), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), eq(PublicKey.from("SENDER".getBytes())));
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), eq(PublicKey.from("RECEIVER".getBytes())));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(PublicKey.from("SENDER".getBytes())));
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(PublicKey.from("RECEIVER".getBytes())));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendSignedTransactionWithDuplicateRecipients() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        new MessageHash("HASH".getBytes()),
                        "ENCRYPTED_PAYLOAD".getBytes(),
                        "ENCRYPTED_KEY".getBytes(),
                        "NONCE".getBytes(),
                        "SENDER".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedRawTransaction));
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));
        when(enclave.encryptPayload(any(RawTransaction.class), any(), any(), any(), any())).thenReturn(payload);

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any(), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), eq(PublicKey.from("SENDER".getBytes())));
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), eq(PublicKey.from("RECEIVER".getBytes())));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(PublicKey.from("SENDER".getBytes())));
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(PublicKey.from("RECEIVER".getBytes())));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendSignedTransactionNoRawTransactionFoundException() {

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());

        try {
            transactionManager.sendSignedTransaction(sendSignedRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getForwardingKeys();
        }
    }

    @Test
    public void delete() {

        String encodedKey = Base64.getEncoder().encodeToString("SOMEKEY".getBytes());

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(encodedKey);
        transactionManager.delete(deleteRequest);

        verify(encryptedTransactionDAO).delete(any(MessageHash.class));
    }

    @Ignore
    @Test
    public void storePayloadAsRecipient() {

        byte[] input = "SOMEDATA".getBytes();

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(payloadEncoder.decode(input)).thenReturn(payload);

        transactionManager.storePayload(input);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Ignore
    @Test
    public void storePayloadWhenWeAreSender() {
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getRecipientBoxes()).thenReturn(new ArrayList<>());
        when(encodedPayload.getRecipientKeys()).thenReturn(new ArrayList<>());

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

        transactionManager.storePayload(input);

        verify(resendManager).acceptOwnMessage(input);
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Ignore
    public void storePayloadWhenWeAreSenderWithPrivateStateConsensus() {
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final byte[] input = "SOMEDATA".getBytes();

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getCipherTextNonce()).thenReturn(null);
        when(encodedPayload.getRecipientBoxes()).thenReturn(emptyList());
        when(encodedPayload.getRecipientNonce()).thenReturn(null);
        when(encodedPayload.getRecipientKeys()).thenReturn(emptyList());
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(encodedPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
        when(encodedPayload.getExecHash()).thenReturn(new byte[0]);

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

        transactionManager.storePayload(input);

        verify(resendManager).acceptOwnMessage(input);
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Ignore
    @Test
    public void storePayloadAsRecipientWithPrivateStateConsensus() {

        byte[] input = "SOMEDATA".getBytes();

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        when(payloadEncoder.decode(input)).thenReturn(payload);

        transactionManager.storePayload(input);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Ignore
    @Test
    public void storePayloadAsRecipientWithAffectedContractTxsButPsvFlagMismatched() {

        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                SecurityHash.from("securityHash".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(senderKey));

        when(payloadEncoder.decode(input)).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any())).thenReturn(Optional.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(input);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(payloadEncoder, times(2)).decode(any());
    }

    @Ignore
    @Test
    public void storePayloadSenderNotGenuineACOTHNotFound() {
        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        final TxHash txHash =
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(txHash, SecurityHash.from("securityHash".getBytes()));
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSr5J5hQ=="),
                SecurityHash.from("bogus".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(senderKey));

        when(payloadEncoder.decode(input)).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(new MessageHash(txHash.getBytes())))
                .thenReturn(Optional.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(input);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO, times(2)).retrieveByHash(any());
        verify(payloadEncoder, times(2)).decode(any());
    }

    @Ignore
    @Test
    public void storePayloadSenderNotInRecipientList() {
        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PublicKey someOtherKey = PublicKey.from("otherKey".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        final TxHash txHash =
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(txHash, SecurityHash.from("securityHash".getBytes()));
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSr5J5hQ=="),
            SecurityHash.from("bogus".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(someOtherKey));

        when(payloadEncoder.decode(input)).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any())).thenReturn(Optional.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(input);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO, times(2)).retrieveByHash(any());
        verify(payloadEncoder, times(3)).decode(any());
    }

    @Ignore
    @Test
    public void storePayloadPsvWithInvalidSecurityHashes() {
        byte[] input = "SOMEDATA".getBytes();

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        when(payloadEncoder.decode(input)).thenReturn(payload);

        when(enclave.findInvalidSecurityHashes(any(), any()))
                .thenReturn(singleton(new TxHash("invalidHash".getBytes())));

        assertThatExceptionOfType(PrivacyViolationException.class)
                .describedAs("There are privacy violation for psv")
                .isThrownBy(() -> transactionManager.storePayload(input))
                .withMessageContaining("Invalid security hashes identified for PSC TX");

        verify(payloadEncoder).decode(input);
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Ignore
    @Test
    public void storePayloadWithInvalidSecurityHashesIgnoreIfNotPsv() {

        final byte[] input = "SOMEDATA".getBytes();

        Map<TxHash, SecurityHash> affectedTx = Map.of(TxHash.from("invalidHash".getBytes()), SecurityHash.from("security".getBytes()));

        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getSenderKey()).thenReturn(PublicKey.from("sender".getBytes()));
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getCipherTextNonce()).thenReturn(new Nonce("nonce".getBytes()));

        RecipientBox recipientBox = mock(RecipientBox.class);
        when(recipientBox.getData()).thenReturn("box1".getBytes());

        when(payload.getRecipientBoxes()).thenReturn(List.of(recipientBox));
        when(payload.getRecipientNonce()).thenReturn(new Nonce("recipientNonce".getBytes()));
        when(payload.getRecipientKeys()).thenReturn(singletonList(PublicKey.from("recipient".getBytes())));
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedTx);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());

        when(payloadEncoder.decode(input)).thenReturn(payload);

        ArgumentCaptor<EncodedPayload> payloadCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        when(enclave.findInvalidSecurityHashes(any(), any()))
                .thenReturn(singleton(new TxHash("invalidHash".getBytes())));

        transactionManager.storePayload(input);

        verify(payloadEncoder).encode(payloadCaptor.capture());
        EncodedPayload sanitisedPayload = payloadCaptor.getValue();

        // Assert that the invalid ACOTH had been removed
        assertThat(sanitisedPayload.getAffectedContractTransactions()
            .get(TxHash.from("invalidHash".getBytes())))
                .isNull();

        verify(encryptedTransactionDAO).retrieveByHash(any());
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).decode(input);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientExists() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);

        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.withRecipient(any(), any())).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(payload, recipientKey)).thenReturn(new byte[0]);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(senderKey));
        verify(enclave, times(2)).getPublicKeys();
        verify(enclave).unencryptTransaction(payload, recipientKey);
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(payloadEncoder).withRecipient(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientsListIsNotEmpty() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);

        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(Collections.singletonList(recipientKey));
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(senderKey));
        verify(enclave).getPublicKeys();
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(payloadEncoder, never()).withRecipient(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void resendAllWithNoValidTransactions() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(emptyList());

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
    }

    @Test
    public void resendAllWhereRequestedIsRecipient() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(payload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(recipientKey));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRecipientInKeyList() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(payload);

        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadEncoder).forRecipient(payload, recipientKey);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsRecipientThenPublishedPayloadDoesNotContainDataForOtherRecipients() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());
        final List<PublicKey> recipients = List.of(recipientKey,anotherRecipient);

        final RecipientBox recipientBox = RecipientBox.from("box1".getBytes());
        final RecipientBox anotherRecipientBox = RecipientBox.from("box2".getBytes());

        final List<RecipientBox> recipientBoxes = List.of(recipientBox,anotherRecipientBox);

        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        EncodedPayload prunedPayload = mock(EncodedPayload.class);
        when(prunedPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
        when(prunedPayload.getRecipientBoxes()).thenReturn(singletonList(recipientBox));
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(prunedPayload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        verify(partyInfoService).publishPayload(eq(prunedPayload), eq(recipientKey));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(payloadEncoder).decode(encodedData);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsSenderThenPublishedPayloadIsNotPruned() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final RecipientBox recipientBox = RecipientBox.from("box1".getBytes());
        final List<RecipientBox> recipientBoxes = List.of(recipientBox);

        final PublicKey localKey = PublicKey.from("LOCAL_KEY".getBytes());
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final List<PublicKey> recipients = new ArrayList<>();

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);
        when(payload.getCipherText()).thenReturn("ciphertext".getBytes());

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.withRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(localKey));

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        ArgumentCaptor<EncodedPayload> epAC = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(partyInfoService).publishPayload(epAC.capture(), eq(senderKey));
        EncodedPayload ep = epAC.getValue();
        verify(payloadEncoder).withRecipient(payload, localKey);
        assertThat(ep.getRecipientBoxes()).hasSize(1).containsExactly(recipientBox);
        assertThat(ep.getSenderKey()).isEqualTo(senderKey);
        assertThat(ep.getCipherText()).containsExactly("ciphertext".getBytes());
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(enclave, times(2)).getPublicKeys();
        verify(enclave).unencryptTransaction(payload, localKey);
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientDoesntExist() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());

        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payload.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(enclave.getPublicKeys()).thenReturn(emptySet());

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        final Throwable throwable = catchThrowable(() -> transactionManager.resend(resendRequest));

        assertThat(throwable)
                .isInstanceOf(RecipientKeyNotFoundException.class)
                .hasMessage("No key found as recipient of message Q0lQSEVSVEVYVA==");

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(1)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWithOnePayloadAndOneRecipientThenPublishPayloadExceptionIsCaught() {

        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Collections.singletonList(encryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(any())).thenReturn(encodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(payloadEncoder).decode(any(byte[].class));
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllIfTwoPayloadsAndFirstThrowsExceptionThenSecondIsStillPublished() {
        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        EncryptedTransaction otherEncryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Arrays.asList(encryptedTransaction, otherEncryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        byte[] otherTransactionBytes = "OTHER_TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);
        when(otherEncryptedTransaction.getEncodedPayload()).thenReturn(otherTransactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload otherEncodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(transactionBytes)).thenReturn(encodedPayload);
        when(payloadEncoder.decode(otherTransactionBytes)).thenReturn(otherEncodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(eq(otherEncodedPayload), any(PublicKey.class)))
                .thenReturn(otherEncodedPayload);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(partyInfoService).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void resendAllTwoPayloadsAndTwoRecipientsAllThrowExceptionButAllStillPublished() {
        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        EncryptedTransaction otherEncryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Arrays.asList(encryptedTransaction, otherEncryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        byte[] otherTransactionBytes = "OTHER_TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);
        when(otherEncryptedTransaction.getEncodedPayload()).thenReturn(otherTransactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload otherEncodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(transactionBytes)).thenReturn(encodedPayload);
        when(payloadEncoder.decode(otherTransactionBytes)).thenReturn(otherEncodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        when(payloadEncoder.forRecipient(encodedPayload, publicKey)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(otherEncodedPayload, publicKey)).thenReturn(otherEncodedPayload);

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        doThrow(new PublishPayloadException("msg"))
                .when(partyInfoService)
                .publishPayload(otherEncodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(partyInfoService).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void resendIndividualNoExistingTransactionFound() {

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from(publicKeyData.getBytes());

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(new String(keyData));
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);

        try {
            transactionManager.resend(resendRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        }
    }

    @Test
    public void resendIndividual() {

        final byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(null, encodedPayloadData);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList(RecipientBox.from("RECIPIENTBOX".getBytes())));

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey recipientKey = PublicKey.from("PUBLICKEY".getBytes());

        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(encodedPayload, recipientKey)).thenReturn(encodedPayload);

        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedOutcome);

        final String messageHashb64 = Base64.getEncoder().encodeToString("KEY".getBytes());

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(messageHashb64);
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).forRecipient(encodedPayload, recipientKey);
        verify(payloadEncoder).encode(encodedPayload);
    }

    @Test
    public void resendIndividualAsSender() {

        final byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(null, encodedPayloadData);

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList(RecipientBox.from("RECIPIENTBOX".getBytes())));
        when(encodedPayload.getRecipientKeys()).thenReturn(new ArrayList<>());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedOutcome);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(any(), any())).thenReturn(new byte[0]);

        final String messageHashb64 = Base64.getEncoder().encodeToString("KEY".getBytes());

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(messageHashb64);
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);

        final ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);

        final ArgumentCaptor<EncodedPayload> captor = ArgumentCaptor.forClass(EncodedPayload.class);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).encode(captor.capture());
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), any());
        assertThat(captor.getValue().getRecipientKeys()).containsExactly(recipientKey);
    }

    @Test
    public void receive() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);

        MessageHash messageHash = new MessageHash(keyData);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();

        assertThat(receiveResponse.getPayload()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getExecHash()).isEqualTo("execHash");

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveRawTransaction() {
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        receiveRequest.setRaw(true);

        MessageHash messageHash = new MessageHash(Base64.getDecoder().decode(keyData));

        final EncryptedRawTransaction encryptedTransaction =
                new EncryptedRawTransaction(
                        messageHash, "payload".getBytes(), "key".getBytes(), "nonce".getBytes(), "sender".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(messageHash)).thenReturn(Optional.of(encryptedTransaction));

        when(enclave.unencryptRawPayload(any(RawTransaction.class))).thenReturn("response".getBytes());

        ReceiveResponse response = transactionManager.receive(receiveRequest);

        assertThat(response.getPayload()).isEqualTo("response".getBytes());

        verify(enclave).unencryptRawPayload(any(RawTransaction.class));
    }

    @Test
    public void receiveRawTransactionNotFound() {
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        receiveRequest.setRaw(true);

        MessageHash messageHash = new MessageHash(Base64.getDecoder().decode(keyData));

        when(encryptedRawTransactionDAO.retrieveByHash(messageHash)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TransactionNotFoundException.class)
                .isThrownBy(() -> transactionManager.receive(receiveRequest));
    }

    @Test
    public void receiveWithAffectedContractTransactions() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);

        MessageHash messageHash = new MessageHash(keyData);

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        final String b64AffectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";
        final Map<TxHash, SecurityHash> affectedTxs = Map.of(new TxHash(b64AffectedTxHash), SecurityHash.from("encoded".getBytes()));

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedTxs);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();

        assertThat(receiveResponse.getPayload()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getExecHash()).isEqualTo("execHash");
        assertThat(receiveResponse.getAffectedContractTransactions()).hasSize(1).containsExactly(b64AffectedTxHash);

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveNoTransactionInDatabase() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        }
    }

    @Test
    public void receiveNoRecipientKeyFound() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);

        MessageHash messageHash = new MessageHash(keyData);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
        }
    }

    @Test
    public void receiveHH() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);

        MessageHash messageHash = new MessageHash(keyData);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, null);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload).thenReturn(null);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        final Throwable throwable = catchThrowable(() -> transactionManager.receive(receiveRequest));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void receiveNullRecipientThrowsNoRecipientKeyFound() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));

        MessageHash messageHash = new MessageHash(keyData);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
        }
    }

    @Test
    public void receiveEmptyRecipientThrowsNoRecipientKeyFound() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo("");
        MessageHash messageHash = new MessageHash(keyData);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
        }
    }

    @Test
    public void storeRaw() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction =
                new RawTransaction(
                        "CIPHERTEXT".getBytes(),
                        "SomeKey".getBytes(),
                        new Nonce("nonce".getBytes()),
                        PublicKey.from(sender));
        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = new StoreRawRequest();
        sendRequest.setFrom(sender);
        sendRequest.setPayload(payload);
        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        StoreRawResponse result = transactionManager.store(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).containsExactly(expectedHash.getHashBytes());

        verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
        verify(encryptedRawTransactionDAO)
                .save(
                        argThat(
                                et -> {
                                    assertThat(et.getEncryptedKey()).containsExactly("SomeKey".getBytes());
                                    assertThat(et.getEncryptedPayload()).containsExactly("CIPHERTEXT".getBytes());
                                    assertThat(et.getHash()).isEqualTo(expectedHash);
                                    assertThat(et.getNonce()).containsExactly("nonce".getBytes());
                                    assertThat(et.getSender()).containsExactly(sender);
                                    return true;
                                }));
    }

    @Test
    public void storeRawWithEmptySender() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction =
                new RawTransaction(
                        "CIPHERTEXT".getBytes(),
                        "SomeKey".getBytes(),
                        new Nonce("nonce".getBytes()),
                        PublicKey.from(sender));
        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);
        when(enclave.defaultPublicKey()).thenReturn(PublicKey.from(sender));
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = new StoreRawRequest();
        sendRequest.setPayload(payload);
        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        StoreRawResponse result = transactionManager.store(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).containsExactly(expectedHash.getHashBytes());

        verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
        verify(enclave).defaultPublicKey();

        verify(encryptedRawTransactionDAO)
                .save(
                        argThat(
                                et -> {
                                    assertThat(et.getEncryptedKey()).containsExactly("SomeKey".getBytes());
                                    assertThat(et.getEncryptedPayload()).containsExactly("CIPHERTEXT".getBytes());
                                    assertThat(et.getHash()).isEqualTo(expectedHash);
                                    assertThat(et.getNonce()).containsExactly("nonce".getBytes());
                                    assertThat(et.getSender()).containsExactly(sender);
                                    return true;
                                }));
    }

    @Test
    public void constructWithLessArgs() {
        final MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setApp(AppType.P2P);
        config.setServerConfigs(Arrays.asList(serverConfig));

        serviceLocator.setServices(
                Stream.of(config, partyInfoService, enclave, partyInfoService).collect(Collectors.toSet()));

        TransactionManager tm =
                new TransactionManagerImpl(
                        encryptedTransactionDAO,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        partyInfoService,
                        1000);

        assertThat(tm).isNotNull();
    }

    @Test
    public void publishDoesNotPublishToSender() {

        TransactionManagerImpl impl = TransactionManagerImpl.class.cast(transactionManager);
        EncodedPayload transaction = mock(EncodedPayload.class);

        PublicKey someKey = mock(PublicKey.class);

        List<PublicKey> recipients = Arrays.asList(someKey);

        when(enclave.getPublicKeys()).thenReturn(singleton(someKey));

        impl.publish(recipients, transaction);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void isSenderThrowsOnMissingTransaction() {
        final String key = Base64.getEncoder().encodeToString("DUMMY_TRANSACTION".getBytes());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        final Throwable throwable = catchThrowable(() -> transactionManager.isSender(key));

        assertThat(throwable)
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Message with hash RFVNTVlfVFJBTlNBQ1RJT04= was not found");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void isSenderReturnsFalseIfNotSender() {
        final String key = Base64.getEncoder().encodeToString("DUMMY_TRANSACTION".getBytes());

        final PublicKey senderKey = PublicKey.from("PUBLIC_KEY".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(new MessageHash(input), input);
        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(new ArrayList<>())
                        .build();

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(emptySet());

        final boolean isSender = transactionManager.isSender(key);

        assertThat(isSender).isFalse();

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void isSenderReturnsTrueIfSender() {
        final String key = Base64.getEncoder().encodeToString("DUMMY_TRANSACTION".getBytes());

        final PublicKey senderKey = PublicKey.from("PUBLIC_KEY".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(new MessageHash(input), input);
        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(new ArrayList<>())
                        .build();

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

        final boolean isSender = transactionManager.isSender(key);

        assertThat(isSender).isTrue();

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void getParticipantsThrowsOnMissingTransaction() {
        final String key = Base64.getEncoder().encodeToString("DUMMY_TRANSACTION".getBytes());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        final Throwable throwable = catchThrowable(() -> transactionManager.getParticipants(key));

        assertThat(throwable)
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Message with hash RFVNTVlfVFJBTlNBQ1RJT04= was not found");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void getParticipantsReturnsAllRecipients() {
        final String key = Base64.getEncoder().encodeToString("DUMMY_TRANSACTION".getBytes());

        final PublicKey senderKey = PublicKey.from("PUBLIC_KEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTS".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(new MessageHash(input), input);
        final EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(senderKey)
                        .withCipherText("CIPHERTEXT".getBytes())
                        .withRecipientBoxes(new ArrayList<>())
                        .withRecipientKeys(Arrays.asList(senderKey, recipientKey))
                        .build();

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);

        final List<PublicKey> participants = transactionManager.getParticipants(key);

        assertThat(participants).containsExactlyInAnyOrder(senderKey, recipientKey);

        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }
}
