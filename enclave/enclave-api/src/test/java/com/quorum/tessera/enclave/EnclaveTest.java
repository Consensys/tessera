package com.quorum.tessera.enclave;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.*;
import com.quorum.tessera.service.Service;
import java.nio.ByteBuffer;
import java.util.*;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnclaveTest {

  private Enclave enclave;

  private Encryptor nacl;

  private KeyManager keyManager;

  @Before
  public void onSetUp() {
    this.nacl = mock(Encryptor.class);
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

  // Case 1
  @Test
  public void unencryptPSVTransaction() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("RecipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(List.of(recipientKey, senderKey))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("EXEC_HASH".getBytes())
            .build();

    final PrivateKey recipientPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(recipientPrivateKey);

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, recipientPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, recipientKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, recipientPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
  }

  // Case 1 - error with unknown key
  @Test
  public void unencryptPSVTransactionWithUnknownKey() {
    final PublicKey nonRecipientKey = PublicKey.from("unknown".getBytes());

    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("RecipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(List.of(recipientKey, senderKey))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("EXEC_HASH".getBytes())
            .build();

    final Throwable throwable =
        catchThrowable(() -> enclave.unencryptTransaction(payload, nonRecipientKey));

    assertThat(throwable)
        .isInstanceOf(EnclaveException.class)
        .hasMessage("recipient not found in listed keys");
  }

  // Case 2
  @Test
  public void unencryptNoRecipientsSingleBoxTransaction() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("RecipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey recipientPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(recipientPrivateKey);

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, recipientPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, recipientKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, recipientPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
  }

  // Case 3 - decrypt using the recipient key
  @Test
  public void unencryptTransactionWeSentUsingRecipientKey() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final RecipientBox senderBox = RecipientBox.from("senderBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(senderBox.getData(), recipientBox.getData()))
            .withRecipientKeys(List.of(senderKey, recipientKey))
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey recipientPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(recipientPrivateKey);

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, recipientPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, recipientKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, recipientPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
  }

  // Case 3 - decrypt using the sender key
  @Test
  public void unencryptTransactionWeSentUsingSenderKey() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final RecipientBox senderBox = RecipientBox.from("senderBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(senderBox.getData(), recipientBox.getData()))
            .withRecipientKeys(List.of(senderKey, recipientKey))
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey senderPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, senderPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(senderBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, senderKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(senderBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, senderPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(senderKey);
  }

  // Case 3 - error when decrypt using the unknown key
  @Test
  public void unencryptTransactionWeSentUsingUnknownKey() {
    final PublicKey nonRecipientKey = PublicKey.from("unknown".getBytes());

    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final RecipientBox senderBox = RecipientBox.from("senderBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(senderBox.getData(), recipientBox.getData()))
            .withRecipientKeys(List.of(senderKey, recipientKey))
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final Throwable throwable =
        catchThrowable(() -> enclave.unencryptTransaction(payload, nonRecipientKey));

    assertThat(throwable)
        .isInstanceOf(EnclaveException.class)
        .hasMessage("recipient not found in listed keys");
  }

  // Case 5 - decrypt normal transaction which we didn't send
  @Test
  public void unencryptTransactionFromOtherNode() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientKey(recipientKey)
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey recipientPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(recipientPrivateKey);
    when(keyManager.getPublicKeys()).thenReturn(Set.of(recipientKey));

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, recipientPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, recipientKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, recipientPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
    verify(keyManager).getPublicKeys();
  }

  // Case 5 - error case where wrong key is used
  @Test
  public void unencryptTransactionFromOtherNodeUsingWrongKey() {
    final PublicKey nonRecipientKey = PublicKey.from("unknown".getBytes());

    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientKey(recipientKey)
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    when(keyManager.getPublicKeys()).thenReturn(Set.of(recipientKey));

    final Throwable throwable =
        catchThrowable(() -> enclave.unencryptTransaction(payload, nonRecipientKey));

    assertThat(throwable)
        .isInstanceOf(EnclaveException.class)
        .hasMessage("recipient not found in listed keys");

    verify(keyManager).getPublicKeys();
  }

  // Case 4.1 - decrypt transaction we sent from a very early version
  // where the sender didn't get an entry
  @Test
  public void unencryptTransactionFromEarlyVersionUsingSender() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientKey(recipientKey)
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey senderPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);
    when(keyManager.getPublicKeys()).thenReturn(Set.of(senderKey));

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(recipientKey, senderPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, senderKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(recipientKey, senderPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(senderKey);
    verify(keyManager).getPublicKeys();
  }

  // Case 4.2 - decrypt transaction we sent from a very early version
  // where the sender didn't get an entry
  @Test
  public void unencryptTransactionFromEarlyVersionUsingRecipient() {
    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientKey(recipientKey)
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    final PrivateKey recipientPrivateKey = PrivateKey.from("private-key".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(recipientPrivateKey);
    when(keyManager.getPublicKeys()).thenReturn(Set.of(senderKey, recipientKey));

    final SharedKey sharedKey = SharedKey.from("shared-key".getBytes());
    when(nacl.computeSharedKey(senderKey, recipientPrivateKey)).thenReturn(sharedKey);

    final byte[] expectedOutcome = "SUCCESS".getBytes();

    when(nacl.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey))
        .thenReturn("sharedOrMasterKeyBytes".getBytes());
    when(nacl.openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class)))
        .thenReturn(expectedOutcome);

    final byte[] result = enclave.unencryptTransaction(payload, recipientKey);

    assertThat(result).isNotNull().isSameAs(expectedOutcome);

    verify(nacl).openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);
    verify(nacl)
        .openAfterPrecomputation(any(byte[].class), eq(cipherTextNonce), any(MasterKey.class));
    verify(nacl).computeSharedKey(senderKey, recipientPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
    verify(keyManager).getPublicKeys();
  }

  // Case 4.2 - error where given key wasn't recipient
  @Test
  public void unencryptTransactionFromEarlyVersionUsingUnknownRecipient() {
    final PublicKey nonRecipientKey = PublicKey.from("unknown".getBytes());

    final PublicKey senderKey = PublicKey.from("senderKey".getBytes());
    final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);
    final RecipientBox recipientBox = RecipientBox.from("recipientBox".getBytes());
    final Nonce recipientNonce = mock(Nonce.class);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox.getData())
            .withRecipientKey(recipientKey)
            .withRecipientNonce(recipientNonce)
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();

    when(keyManager.getPublicKeys()).thenReturn(Set.of(senderKey, recipientKey));

    final Throwable throwable =
        catchThrowable(() -> enclave.unencryptTransaction(payload, nonRecipientKey));

    assertThat(throwable)
        .isInstanceOf(EnclaveException.class)
        .hasMessage("recipient not found in listed keys");

    verify(keyManager).getPublicKeys();
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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create().withPrivacyMode(PrivacyMode.STANDARD_PRIVATE).build();

    EncodedPayload result =
        enclave.encryptPayload(
            message, senderPublicKey, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isNotPresent();

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
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
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
    when(nacl.openAfterPrecomputation(closedbox.getData(), recipientNonce, sharedKey))
        .thenReturn(openbox);
    byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderPublicKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(affectedTxPayload.getRecipientNonce()).thenReturn(recipientNonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientPublicKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    TxHash txnHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txnHash);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);

    List<AffectedTransaction> affectedContractTransactions = List.of(affectedTransaction);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedTransactions(affectedContractTransactions)
            .build();

    final EncodedPayload result =
        enclave.encryptPayload(
            message, senderPublicKey, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getAffectedContractTransactions()).containsOnlyKeys(txnHash);

    verify(nacl).createMasterKey();
    verify(nacl, times(2)).randomNonce();
    verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
    verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
    verify(nacl).openAfterPrecomputation(closedbox.getData(), recipientNonce, sharedKey);
    verify(nacl, times(2)).computeSharedKey(recipientPublicKey, senderPrivateKey);
    verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
    verify(keyManager).getPublicKeys();
  }

  @Test
  public void encryptPayloadWithMandatoryRecipients() {

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withMandatoryRecipients(Set.of(recipientPublicKey))
            .build();

    EncodedPayload result =
        enclave.encryptPayload(
            message, senderPublicKey, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).hasSize(1);
    assertThat(result.getMandatoryRecipients()).containsExactly(recipientPublicKey);

    verify(nacl).createMasterKey();
    verify(nacl, times(2)).randomNonce();
    verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
    verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
    verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
  }

  @Test
  public void encryptPayloadWithPrivacyGroupId() {

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("GROUP".getBytes()))
            .build();

    EncodedPayload result =
        enclave.encryptPayload(
            message, senderPublicKey, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get())
        .isEqualTo(PrivacyGroup.Id.fromBytes("GROUP".getBytes()));

    verify(nacl).createMasterKey();
    verify(nacl, times(2)).randomNonce();
    verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
    verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
    verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
    verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
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

    RawTransaction rawTransaction =
        new RawTransaction(cipherText, encryptedKeyBytes, cipherNonce, senderPublicKey);

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create().withPrivacyMode(PrivacyMode.STANDARD_PRIVATE).build();

    EncodedPayload result =
        enclave.encryptPayload(rawTransaction, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isNotPresent();

    verify(nacl).randomNonce();
    verify(nacl).openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender);
    verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
    verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
    verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
    verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
  }

  @Test
  public void encryptPayloadRawTransactionWithMandatoryRecipients() {

    byte[] message = "MESSAGE".getBytes();

    byte[] masterKeyBytes = "masterKeyBytes".getBytes();
    MasterKey masterKey = MasterKey.from(masterKeyBytes);
    PublicKey senderPublicKey = PublicKey.from("SENDER".getBytes());
    PrivateKey senderPrivateKey = mock(PrivateKey.class);

    PublicKey recipientPublicKey = PublicKey.from("RECIPIENT".getBytes());
    Nonce cipherNonce = new Nonce("NONCE".getBytes());
    byte[] cipherText = "cipherText".getBytes();
    byte[] encryptedKeyBytes = "ENCRYPTED_KEY".getBytes();

    RawTransaction rawTransaction =
        new RawTransaction(cipherText, encryptedKeyBytes, cipherNonce, senderPublicKey);

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withMandatoryRecipients(Set.of(recipientPublicKey))
            .build();

    EncodedPayload result =
        enclave.encryptPayload(rawTransaction, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).containsExactly(recipientPublicKey);

    verify(nacl).randomNonce();
    verify(nacl).openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender);
    verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
    verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
    verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
    verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
  }

  @Test
  public void encryptPayloadRawTransactionWithPrivacyGroupId() {

    byte[] message = "MESSAGE".getBytes();

    byte[] masterKeyBytes = "masterKeyBytes".getBytes();
    MasterKey masterKey = MasterKey.from(masterKeyBytes);
    PublicKey senderPublicKey = PublicKey.from("SENDER".getBytes());
    PrivateKey senderPrivateKey = mock(PrivateKey.class);

    PublicKey recipientPublicKey = PublicKey.from("RECIPIENT".getBytes());
    Nonce cipherNonce = new Nonce("NONCE".getBytes());
    byte[] cipherText = "cipherText".getBytes();
    byte[] encryptedKeyBytes = "ENCRYPTED_KEY".getBytes();

    RawTransaction rawTransaction =
        new RawTransaction(cipherText, encryptedKeyBytes, cipherNonce, senderPublicKey);

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey))
        .thenReturn(encryptedMasterKeys);

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()))
            .build();

    EncodedPayload result =
        enclave.encryptPayload(rawTransaction, Arrays.asList(recipientPublicKey), metaData);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
    assertThat(result.getCipherText()).isEqualTo(cipherText);
    assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
    assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
    assertThat(result.getRecipientBoxes()).containsExactly(RecipientBox.from(encryptedMasterKeys));
    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get())
        .isEqualTo(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()));

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
    when(nacl.sealAfterPrecomputation(masterKeyBytes, cipherNonce, sharedKey))
        .thenReturn(encryptedMasterKey);

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

    final Throwable throwable =
        catchThrowable(() -> enclave.createNewRecipientBox(payload, publicKey));

    assertThat(throwable)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No key or recipient-box to use");
  }

  @Test
  public void createNewRecipientBoxWithExistingNoRecipientBoxes() {

    final PublicKey publicKey = PublicKey.from(new byte[0]);

    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(null);
    when(payload.getCipherText()).thenReturn(null);
    when(payload.getCipherTextNonce()).thenReturn(null);
    when(payload.getRecipientBoxes()).thenReturn(emptyList());
    when(payload.getRecipientNonce()).thenReturn(null);
    when(payload.getRecipientKeys()).thenReturn(singletonList(publicKey));

    final Throwable throwable =
        catchThrowable(() -> enclave.createNewRecipientBox(payload, publicKey));

    assertThat(throwable)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No key or recipient-box to use");
  }

  @Test
  public void createNewRecipientBoxGivesBackSuccessfulEncryptedKey() {

    final PublicKey publicKey = PublicKey.from("recipient".getBytes());
    final PublicKey senderKey = PublicKey.from("sender".getBytes());
    final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());
    final SharedKey recipientSenderShared = SharedKey.from("shared-one".getBytes());
    final SharedKey senderShared = SharedKey.from("shared-two".getBytes());
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
    final byte[] openbox = "open".getBytes();
    final Nonce nonce = new Nonce("nonce".getBytes());

    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(null);
    when(payload.getCipherTextNonce()).thenReturn(null);
    when(payload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(publicKey));

    when(nacl.computeSharedKey(publicKey, privateKey)).thenReturn(recipientSenderShared);
    when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(senderShared);
    when(nacl.openAfterPrecomputation(closedbox.getData(), nonce, recipientSenderShared))
        .thenReturn(openbox);
    when(nacl.sealAfterPrecomputation(openbox, nonce, senderShared))
        .thenReturn("newbox".getBytes());
    when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(privateKey);

    final byte[] newRecipientBox = enclave.createNewRecipientBox(payload, senderKey);

    assertThat(newRecipientBox).containsExactly("newbox".getBytes());

    verify(nacl).computeSharedKey(publicKey, privateKey);
    verify(nacl).computeSharedKey(senderKey, privateKey);
    verify(nacl).openAfterPrecomputation(closedbox.getData(), nonce, recipientSenderShared);
    verify(nacl).sealAfterPrecomputation(openbox, nonce, senderShared);
    verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderKey);
  }

  @Test
  public void findInvalidSecurityHashesTransactionSentToCurrentNode() {

    final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
    final PublicKey senderKey = PublicKey.from("sender".getBytes());
    final PrivateKey privateKey = PrivateKey.from("private".getBytes());

    final SharedKey sharedKey = SharedKey.from("shared".getBytes());
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
    final byte[] openbox = "open".getBytes();
    final Nonce nonce = new Nonce("nonce".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);

    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);

    when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);
    when(nacl.openAfterPrecomputation(closedbox.getData(), nonce, sharedKey)).thenReturn(openbox);
    when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

    when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

    TxHash txHash =
        TxHash.from(
            Base64.getDecoder()
                .decode(
                    "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="));
    Map<TxHash, SecurityHash> affectedContractTransactionHashes =
        Map.of(txHash, SecurityHash.from("securityHash".getBytes()));

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(cipherText);
    when(payload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(payload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
    when(payload.getExecHash()).thenReturn(new byte[0]);

    final EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(affectedTxPayload.getRecipientNonce()).thenReturn(nonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    var txnHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txnHash);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);

    Set<TxHash> invalidHashes =
        enclave.findInvalidSecurityHashes(payload, List.of(affectedTransaction));

    assertThat(invalidHashes).hasSize(1);

    verify(nacl).computeSharedKey(senderKey, privateKey);
    verify(nacl).openAfterPrecomputation(closedbox.getData(), nonce, sharedKey);
    verify(keyManager, times(2)).getPublicKeys();
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
  }

  @Test
  public void findInvalidSecurityHashesTransactionSentToCurrentNodeAllHashesMatch() {

    final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
    final PublicKey senderKey = PublicKey.from("sender".getBytes());
    final PrivateKey privateKey = PrivateKey.from("private".getBytes());

    final SharedKey sharedKey = SharedKey.from("shared".getBytes());
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
    final byte[] openbox = "open".getBytes();
    final Nonce nonce = new Nonce("nonce".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);

    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);

    when(nacl.computeSharedKey(senderKey, privateKey)).thenReturn(sharedKey);
    when(nacl.openAfterPrecomputation(closedbox.getData(), nonce, sharedKey)).thenReturn(openbox);
    when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

    when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

    final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();

    TxHash txHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

    final Map<TxHash, SecurityHash> affectedContractTransactionHashes =
        Map.of(txHash, SecurityHash.from(digestSHA3.digest("cipherTextcipherTextopen".getBytes())));
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(cipherText);
    when(payload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(payload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
    when(payload.getExecHash()).thenReturn(new byte[0]);

    final EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(affectedTxPayload.getRecipientNonce()).thenReturn(nonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
    affectedContractTransactions.put(txHash, affectedTxPayload);

    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txHash);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);

    Set<TxHash> invalidHashes =
        enclave.findInvalidSecurityHashes(payload, List.of(affectedTransaction));

    assertThat(invalidHashes).hasSize(0);

    verify(nacl).computeSharedKey(senderKey, privateKey);
    verify(nacl).openAfterPrecomputation(closedbox.getData(), nonce, sharedKey);
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

    final Map<TxHash, SecurityHash> affectedContractTransactionHashes =
        Map.of(
            new TxHash(
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
            SecurityHash.from("securityHash".getBytes()));

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(cipherText);
    when(payload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(payload.getRecipientBoxes()).thenReturn(emptyList());
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
    when(payload.getExecHash()).thenReturn(new byte[0]);

    final EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(emptyList());
    when(affectedTxPayload.getRecipientNonce()).thenReturn(nonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    TxHash txHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);
    when(affectedTransaction.getHash()).thenReturn(txHash);
    try {
      enclave.findInvalidSecurityHashes(payload, List.of(affectedTransaction));
    } catch (Throwable e) {
      assertThat(e).isInstanceOf(RuntimeException.class);
      assertThat(e)
          .hasMessageContaining("An EncodedPayload should have at least one recipient box.");
    }
  }

  @Test
  public void findInvalidSecurityHashesTransactionSentFromCurrentNode() {

    final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
    final PublicKey senderKey = PublicKey.from("sender".getBytes());
    final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());

    final SharedKey sharedKey = SharedKey.from("shared".getBytes());
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
    final byte[] openbox = "open".getBytes();
    final Nonce nonce = new Nonce("nonce".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);

    when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(privateKey);

    when(nacl.computeSharedKey(recipientKey, privateKey)).thenReturn(sharedKey);
    when(nacl.openAfterPrecomputation(closedbox.getData(), nonce, sharedKey)).thenReturn(openbox);
    when(nacl.sealAfterPrecomputation(openbox, nonce, sharedKey)).thenReturn("newbox".getBytes());

    when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderKey));

    // compute the security hash
    ByteBuffer byteBuffer = ByteBuffer.allocate(2 * cipherText.length + openbox.length);
    byteBuffer.put(cipherText);
    byteBuffer.put(cipherText);
    byteBuffer.put(openbox);

    final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
    final byte[] securityHash = digestSHA3.digest(byteBuffer.array());

    Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
    affectedContractTransactionHashes.put(
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
        SecurityHash.from(securityHash));
    affectedContractTransactionHashes.put(
        new TxHash(
            "afMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
        SecurityHash.from("securityHash2".getBytes()));
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(cipherText);
    when(payload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(payload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
    when(payload.getExecHash()).thenReturn(new byte[0]);

    final EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(affectedTxPayload.getRecipientNonce()).thenReturn(nonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
    affectedContractTransactions.put(
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
        affectedTxPayload);

    TxHash txHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txHash);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);

    Set<TxHash> invalidHashes =
        enclave.findInvalidSecurityHashes(payload, List.of(affectedTransaction));

    assertThat(invalidHashes).hasSize(1);

    verify(nacl).computeSharedKey(recipientKey, privateKey);
    verify(nacl).openAfterPrecomputation(closedbox.getData(), nonce, sharedKey);
    verify(keyManager, times(1)).getPublicKeys();
    verify(keyManager).getPrivateKeyForPublicKey(senderKey);
  }

  @Test
  public void notAbleToDecryptMasterKey() {
    final PublicKey recipientKey = PublicKey.from("recipient".getBytes());
    final PublicKey senderKey = PublicKey.from("sender".getBytes());
    final PrivateKey privateKey = PrivateKey.from("sender-priv".getBytes());
    final RecipientBox closedbox = RecipientBox.from("closed".getBytes());
    final Nonce nonce = new Nonce("nonce".getBytes());
    final byte[] cipherText = "cipherText".getBytes();
    final Nonce cipherTextNonce = mock(Nonce.class);

    when(keyManager.getPrivateKeyForPublicKey(recipientKey)).thenReturn(privateKey);
    when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(recipientKey));

    when(nacl.computeSharedKey(senderKey, privateKey))
        .thenThrow(new EncryptorException("JNacl could not compute the shared key"));

    Map<TxHash, SecurityHash> affectedContractTransactionHashes =
        Map.of(
            new TxHash(
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
            SecurityHash.from("securityHash".getBytes()));

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(senderKey);
    when(payload.getCipherText()).thenReturn(cipherText);
    when(payload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(payload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(payload.getRecipientNonce()).thenReturn(nonce);
    when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
    when(payload.getExecHash()).thenReturn(new byte[0]);

    final EncodedPayload affectedTxPayload = mock(EncodedPayload.class);
    when(affectedTxPayload.getSenderKey()).thenReturn(senderKey);
    when(affectedTxPayload.getCipherText()).thenReturn(cipherText);
    when(affectedTxPayload.getCipherTextNonce()).thenReturn(cipherTextNonce);
    when(affectedTxPayload.getRecipientBoxes()).thenReturn(singletonList(closedbox));
    when(affectedTxPayload.getRecipientNonce()).thenReturn(nonce);
    when(affectedTxPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
    when(affectedTxPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(affectedTxPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
    when(affectedTxPayload.getExecHash()).thenReturn(new byte[0]);

    TxHash txHash =
        new TxHash(
            "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txHash);
    when(affectedTransaction.getPayload()).thenReturn(affectedTxPayload);

    try {
      enclave.findInvalidSecurityHashes(payload, List.of(affectedTransaction));
      failBecauseExceptionWasNotThrown(any());
    } catch (Throwable ex) {
      assertThat(ex).isInstanceOf(RuntimeException.class);
      assertThat(ex).hasMessageContaining("Unable to decrypt master key");
    }

    verify(keyManager, times(2)).getPublicKeys();
    verify(keyManager).getPrivateKeyForPublicKey(recipientKey);
    verify(nacl).computeSharedKey(senderKey, privateKey);
  }

  @Test
  public void create() {

    Enclave expectedEnclave = mock(Enclave.class);
    Enclave result;
    try (var mockStaticServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader<Enclave> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expectedEnclave));
      mockStaticServiceLoader
          .when(() -> ServiceLoader.load(Enclave.class))
          .thenReturn(serviceLoader);

      result = Enclave.create();
    }

    assertThat(result).isSameAs(expectedEnclave);
  }
}
