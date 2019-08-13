package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.*;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.service.Service;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EnclaveTest {

    private Enclave enclave;

    private NaclFacade nacl;

    private KeyManager keyManager;

    @Before
    public void onSetUp() {
        this.nacl = mock(NaclFacade.class);
        this.keyManager = mock(KeyManager.class);

        this.enclave = new EnclaveImpl(nacl, keyManager);
        enclave.start();
        assertThat(enclave.status()).isEqualTo(Service.Status.STARTED);
    }

    @After
    public void onTearDown() {
        enclave.stop();
        assertThat(enclave.status()).isEqualTo(Service.Status.STARTED);
        verifyNoMoreInteractions(nacl, keyManager);
    }

    @Test
    public void defaultPublicKey() {
        enclave.defaultPublicKey();
        verify(keyManager).defaultPublicKey();
    }

    @Test
    public void getForwardingKeys() {
        enclave.getForwardingKeys();
        verify(keyManager).getForwardingKeys();
    }

    @Test
    public void getPublicKeys() {
        enclave.getPublicKeys();
        verify(keyManager).getPublicKeys();
    }

    @Test
    public void unencryptTransaction() {

        PublicKey senderKey = mock(PublicKey.class);

        PublicKey recipientKey = mock(PublicKey.class);

        PublicKey providedSenderKey = mock(PublicKey.class);

        byte[] cipherText = "cipherText".getBytes();

        Nonce cipherTextNonce = mock(Nonce.class);

        byte[] recipientBox = "RecipientBox".getBytes();

        Nonce recipientNonce = mock(Nonce.class);

        EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(recipientBox),
                        recipientNonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderKey));

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class)))
                .thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payload, providedSenderKey);

        assertThat(result).isNotNull().isSameAs(expectedOutcome);

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class));
        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class));
        verify(keyManager).getPrivateKeyForPublicKey(senderKey);
        verify(keyManager).getPublicKeys();
        verify(nacl).computeSharedKey(recipientKey, senderPrivateKey);
    }

    @Test
    public void unencryptRawPayload() {

        PublicKey senderKey = mock(PublicKey.class);

        byte[] cipherText = "cipherText".getBytes();

        byte[] recipientBox = "RecipientBox".getBytes();

        Nonce nonce = mock(Nonce.class);

        RawTransaction rawTransaction = new RawTransaction(cipherText, recipientBox, nonce, senderKey);

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class)))
                .thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptRawPayload(rawTransaction);

        assertThat(result).isNotNull().isSameAs(expectedOutcome);

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class));
        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class));
        verify(keyManager).getPrivateKeyForPublicKey(senderKey);
        verify(nacl).computeSharedKey(senderKey, senderPrivateKey);
    }

    @Test
    public void unencryptTransactionFromAnotherNode() {

        PublicKey senderKey = mock(PublicKey.class);

        PublicKey recipientKey = mock(PublicKey.class);

        PublicKey providedSenderKey = mock(PublicKey.class);

        byte[] cipherText = "cipherText".getBytes();

        Nonce cipherTextNonce = mock(Nonce.class);

        byte[] recipientBox = "RecipientBox".getBytes();

        Nonce recipientNonce = mock(Nonce.class);

        EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(recipientBox),
                        recipientNonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        when(keyManager.getPublicKeys()).thenReturn(Collections.emptySet());

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(providedSenderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class)))
                .thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payload, providedSenderKey);

        assertThat(result).isNotNull().isSameAs(expectedOutcome);

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class));
        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class));
        verify(keyManager).getPrivateKeyForPublicKey(providedSenderKey);
        verify(keyManager).getPublicKeys();
        verify(nacl).computeSharedKey(senderKey, senderPrivateKey);
    }

    @Test
    public void encryptPayload() {

        byte[] message = "MESSAGE".getBytes();

        PublicKey senderPublicKey = mock(PublicKey.class);
        PublicKey recipientPublicKey = mock(PublicKey.class);

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        Nonce cipherNonce = mock(Nonce.class);
        Nonce recipientNonce = mock(Nonce.class);

        byte[] cipherText = "cipherText".getBytes();

        when(nacl.createMasterKey()).thenReturn(masterKey);
        when(nacl.randomNonce()).thenReturn(cipherNonce, recipientNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        PrivateKey senderPrivateKey = mock(PrivateKey.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey)).thenReturn(encryptedMasterKeys);

        EncodedPayload result =
                enclave.encryptPayload(
                        message,
                        senderPublicKey,
                        Arrays.asList(recipientPublicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        null);

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getCipherText()).isEqualTo(cipherText);
        assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getRecipientBoxes()).containsExactly(encryptedMasterKeys);

        verify(nacl).createMasterKey();
        verify(nacl, times(2)).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
    }

    @Test
    public void encryptPayloadWithAffectedTransactions() {

        byte[] message = "MESSAGE".getBytes();

        PublicKey senderPublicKey = mock(PublicKey.class);
        PublicKey recipientPublicKey = mock(PublicKey.class);

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        Nonce cipherNonce = mock(Nonce.class);
        Nonce recipientNonce = mock(Nonce.class);
        final byte[] closedbox = "closed".getBytes();
        final byte[] openbox = "open".getBytes();
        byte[] cipherText = "cipherText".getBytes();

        when(nacl.createMasterKey()).thenReturn(masterKey);
        when(nacl.randomNonce()).thenReturn(cipherNonce, recipientNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);
        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderPublicKey));

        PrivateKey senderPrivateKey = mock(PrivateKey.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientPublicKey, senderPrivateKey)).thenReturn(sharedKey);
        when(nacl.openAfterPrecomputation(closedbox, recipientNonce, sharedKey)).thenReturn(openbox);
        byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey)).thenReturn(encryptedMasterKeys);

        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderPublicKey,
                        cipherText,
                        cipherNonce,
                        singletonList(closedbox),
                        recipientNonce,
                        singletonList(recipientPublicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                affectedTxPayload);

        final EncodedPayload result =
                enclave.encryptPayload(
                        message,
                        senderPublicKey,
                        Arrays.asList(recipientPublicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        affectedContractTransactions,
                        new byte[0]);

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getCipherText()).isEqualTo(cipherText);
        assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getRecipientBoxes()).containsExactly(encryptedMasterKeys);
        assertThat(result.getAffectedContractTransactions().keySet())
                .hasSize(1)
                .containsExactly(
                        new TxHash(
                                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="));

        verify(nacl).createMasterKey();
        verify(nacl, times(2)).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).openAfterPrecomputation(closedbox, recipientNonce, sharedKey);
        verify(nacl, times(2)).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
        verify(keyManager).getPublicKeys();
    }

    @Test
    public void encryptPayloadRawTransaction() {

        byte[] message = "MESSAGE".getBytes();

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        PublicKey senderPublicKey = PublicKey.from("SENDER".getBytes());
        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        PublicKey recipientPublicKey = PublicKey.from("RECIPIENT".getBytes());
        Nonce cipherNonce = new Nonce("NONCE".getBytes());
        byte[] cipherText = "cipherText".getBytes();
        byte[] encryptedKeyBytes = "ENCRYPTED_KEY".getBytes();

        RawTransaction rawTransaction = new RawTransaction(cipherText, encryptedKeyBytes, cipherNonce, senderPublicKey);

        Nonce recipientNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKeyForSender = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderPublicKey, senderPrivateKey)).thenReturn(sharedKeyForSender);

        when(nacl.openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender))
                .thenReturn(masterKeyBytes);

        when(nacl.randomNonce()).thenReturn(recipientNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey)).thenReturn(encryptedMasterKeys);

        EncodedPayload result =
                enclave.encryptPayload(
                        rawTransaction,
                        Arrays.asList(recipientPublicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getCipherText()).isEqualTo(cipherText);
        assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getRecipientBoxes()).containsExactly(encryptedMasterKeys);

        verify(nacl).randomNonce();
        verify(nacl).openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
        verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
    }

    @Test
    public void encryptRawPayload() {

        byte[] message = "MESSAGE".getBytes();

        PublicKey senderPublicKey = mock(PublicKey.class);

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        Nonce cipherNonce = mock(Nonce.class);

        byte[] cipherText = "cipherText".getBytes();

        when(nacl.createMasterKey()).thenReturn(masterKey);
        when(nacl.randomNonce()).thenReturn(cipherNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        PrivateKey senderPrivateKey = mock(PrivateKey.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKey = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, cipherNonce, sharedKey)).thenReturn(encryptedMasterKey);

        RawTransaction result = enclave.encryptRawPayload(message, senderPublicKey);

        assertThat(result).isNotNull();
        assertThat(result.getFrom()).isEqualTo(senderPublicKey);
        assertThat(result.getEncryptedPayload()).isEqualTo(cipherText);
        assertThat(result.getNonce()).isEqualTo(cipherNonce);
        assertThat(result.getEncryptedKey()).isEqualTo(encryptedMasterKey);

        verify(nacl).createMasterKey();
        verify(nacl).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, cipherNonce, sharedKey);
        verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
    }

    @Test
    public void createNewRecipientBoxWithNoRecipientList() {

        final PublicKey publicKey = PublicKey.from(new byte[0]);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(emptyList());

        final Throwable throwable = catchThrowable(() -> enclave.createNewRecipientBox(payload, publicKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("No key or recipient-box to use");
    }

    @Test
    public void createNewRecipientBoxWithExistingNoRecipientBoxes() {

        final PublicKey publicKey = PublicKey.from(new byte[0]);
        final EncodedPayload payload =
                new EncodedPayload(
                        null,
                        null,
                        null,
                        emptyList(),
                        null,
                        singletonList(publicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        final Throwable throwable = catchThrowable(() -> enclave.createNewRecipientBox(payload, publicKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("No key or recipient-box to use");
    }

    @Test
    public void createNewRecipientBoxGivesBackSuccessfulEncryptedKey() {

        final PublicKey publicKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());
        final SharedKey recipientSenderShared = SharedKey.from("shared-one".getBytes());
        final SharedKey senderShared = SharedKey.from("shared-two".getBytes());
        final byte[] closedbox = "closed".getBytes();
        final byte[] openbox = "open".getBytes();
        final Nonce nonce = new Nonce("nonce".getBytes());

        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        null,
                        null,
                        singletonList(closedbox),
                        nonce,
                        singletonList(publicKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        when(nacl.computeSharedKey(publicKey, privateKey)).thenReturn(recipientSenderShared);
        when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(senderShared);
        when(nacl.openAfterPrecomputation(closedbox, nonce, recipientSenderShared)).thenReturn(openbox);
        when(nacl.sealAfterPrecomputation(openbox, nonce, senderShared)).thenReturn("newbox".getBytes());
        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(privateKey);

        final byte[] newRecipientBox = enclave.createNewRecipientBox(payload, senderKey);

        assertThat(newRecipientBox).containsExactly("newbox".getBytes());

        verify(nacl).computeSharedKey(publicKey, privateKey);
        verify(nacl).computeSharedKey(senderKey, privateKey);
        verify(nacl).openAfterPrecomputation(closedbox, nonce, recipientSenderShared);
        verify(nacl).sealAfterPrecomputation(openbox, nonce, senderShared);
        verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderKey);
    }

    @Test
    public void findInvalidSecurityHashesTransactionSentToCurrentNode() {

        final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("private".getBytes());

        final SharedKey sharedKey = SharedKey.from("shared".getBytes());
        final byte[] closedbox = "closed".getBytes();
        final byte[] openbox = "open".getBytes();
        final Nonce nonce = new Nonce("nonce".getBytes());
        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);

        when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);
        when(nacl.openAfterPrecomputation(closedbox, nonce, sharedKey)).thenReturn(openbox);
        when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                "securityHash".getBytes());
        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        affectedContractTransactionHashes,
                        new byte[0]);

        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                affectedTxPayload);

        Set<TxHash> invalidHashes = enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

        assertThat(invalidHashes).hasSize(1);

        verify(nacl).computeSharedKey(senderKey, privateKey);
        verify(nacl).openAfterPrecomputation(closedbox, nonce, sharedKey);
        verify(keyManager, times(2)).getPublicKeys();
        verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
    }

    @Test
    public void findInvalidSecurityHashesTransactionSentToCurrentNodeAllHashesMatch() {

        final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("private".getBytes());

        final SharedKey sharedKey = SharedKey.from("shared".getBytes());
        final byte[] closedbox = "closed".getBytes();
        final byte[] openbox = "open".getBytes();
        final Nonce nonce = new Nonce("nonce".getBytes());
        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);

        when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);
        when(nacl.openAfterPrecomputation(closedbox, nonce, sharedKey)).thenReturn(openbox);
        when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();

        TxHash txHash =
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(txHash, digestSHA3.digest("cipherTextcipherTextopen".getBytes()));
        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        affectedContractTransactionHashes,
                        new byte[0]);

        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(txHash, affectedTxPayload);

        Set<TxHash> invalidHashes = enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

        assertThat(invalidHashes).hasSize(0);

        verify(nacl).computeSharedKey(senderKey, privateKey);
        verify(nacl).openAfterPrecomputation(closedbox, nonce, sharedKey);
        verify(keyManager, times(2)).getPublicKeys();
        verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
    }

    @Test
    public void findInvalidSecurityHashesTransactionSentToCurrentNodeEmptyRecipientBoxes() {

        final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("private".getBytes());

        final Nonce nonce = new Nonce("nonce".getBytes());
        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                "securityHash".getBytes());
        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        emptyList(),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        affectedContractTransactionHashes,
                        null);

        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        emptyList(),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                affectedTxPayload);

        try {
            enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e).hasMessageContaining("An EncodedPayload should have at least one recipient box.");
        }
    }

    @Test
    public void findInvalidSecurityHashesTransactionSentFromCurrentNode() {

        final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());

        final SharedKey sharedKey = SharedKey.from("shared".getBytes());
        final byte[] closedbox = "closed".getBytes();
        final byte[] openbox = "open".getBytes();
        final Nonce nonce = new Nonce("nonce".getBytes());
        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(privateKey);

        when(nacl.computeSharedKey(recipientKey, privateKey)).thenReturn(sharedKey);
        when(nacl.openAfterPrecomputation(closedbox, nonce, sharedKey)).thenReturn(openbox);
        when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderKey));

        // compute the security hash
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 * cipherText.length + openbox.length);
        byteBuffer.put(cipherText);
        byteBuffer.put(cipherText);
        byteBuffer.put(openbox);

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] securityHash = digestSHA3.digest(byteBuffer.array());

        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                securityHash);
        affectedContractTransactionHashes.put(
                new TxHash("afMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                "securityHash2".getBytes());
        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.PARTY_PROTECTION,
                        affectedContractTransactionHashes,
                        new byte[0]);

        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.PARTY_PROTECTION,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                affectedTxPayload);

        Set<TxHash> invalidHashes = enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

        assertThat(invalidHashes).hasSize(1);

        verify(nacl).computeSharedKey(recipientKey, privateKey);
        verify(nacl).openAfterPrecomputation(closedbox, nonce, sharedKey);
        verify(keyManager, times(1)).getPublicKeys();
        verify(keyManager).getPrivateKeyForPublicKey(senderKey);
    }

    @Test
    public void notAbleToDecryptMasterKey() {
        final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());
        final byte[] closedbox = "closed".getBytes();
        final Nonce nonce = new Nonce("nonce".getBytes());
        final byte[] cipherText = "cipherText".getBytes();
        final Nonce cipherTextNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);
        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

        when(nacl.computeSharedKey(senderKey, privateKey))
                .thenThrow(new NaclException("JNacl could not compute the shared key"));

        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                "securityHash".getBytes());
        final EncodedPayload payload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        affectedContractTransactionHashes,
                        new byte[0]);
        final EncodedPayload affectedTxPayload =
                new EncodedPayload(
                        senderKey,
                        cipherText,
                        cipherTextNonce,
                        singletonList(closedbox),
                        nonce,
                        singletonList(recipientKey),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        affectedContractTransactions.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                affectedTxPayload);

        try {
            enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);
            failBecauseExceptionWasNotThrown(any());
        } catch (Throwable ex) {
            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex).hasMessageContaining("Unable to decrypt master key");
        }

        verify(keyManager, times(2)).getPublicKeys();
        verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
        verify(nacl).computeSharedKey(senderKey, privateKey);
    }
}
