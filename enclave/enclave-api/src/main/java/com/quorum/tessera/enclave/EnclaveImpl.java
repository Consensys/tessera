package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveImpl implements Enclave {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveImpl.class);

  private final Encryptor encryptor;

  private final KeyManager keyManager;

  public EnclaveImpl(Encryptor encryptor, KeyManager keyManager) {
    this.encryptor = Objects.requireNonNull(encryptor);
    this.keyManager = Objects.requireNonNull(keyManager);
  }

  @Override
  public EncodedPayload encryptPayload(
      final byte[] message,
      final PublicKey senderPublicKey,
      final List<PublicKey> recipientPublicKeys,
      final PrivacyMetadata privacyMetadata) {

    final MasterKey masterKey = encryptor.createMasterKey();
    final Nonce nonce = encryptor.randomNonce();
    final Nonce recipientNonce = encryptor.randomNonce();

    final byte[] cipherText = encryptor.sealAfterPrecomputation(message, nonce, masterKey);

    final List<byte[]> encryptedMasterKeys =
        buildRecipientMasterKeys(senderPublicKey, recipientPublicKeys, recipientNonce, masterKey);

    final Map<TxHash, byte[]> affectedContractTransactionHashes =
        buildAffectedContractTransactionHashes(
            privacyMetadata.getAffectedContractTransactions().stream()
                .collect(
                    Collectors.toUnmodifiableMap(
                        AffectedTransaction::getHash, AffectedTransaction::getPayload)),
            cipherText);

    final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();

    privacyMetadata.getPrivacyGroupId().ifPresent(payloadBuilder::withPrivacyGroupId);

    return payloadBuilder
        .withSenderKey(senderPublicKey)
        .withCipherText(cipherText)
        .withCipherTextNonce(nonce)
        .withRecipientBoxes(encryptedMasterKeys)
        .withRecipientNonce(recipientNonce)
        .withRecipientKeys(recipientPublicKeys)
        .withPrivacyMode(privacyMetadata.getPrivacyMode())
        .withAffectedContractTransactions(affectedContractTransactionHashes)
        .withExecHash(privacyMetadata.getExecHash())
        .withMandatoryRecipients(privacyMetadata.getMandatoryRecipients())
        .build();
  }

  private Map<TxHash, byte[]> buildAffectedContractTransactionHashes(
      Map<TxHash, EncodedPayload> affectedContractTransactions, byte[] cipherText) {
    Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
    for (final Map.Entry<TxHash, EncodedPayload> entry : affectedContractTransactions.entrySet()) {
      // TODO - remove extra logs
      LOGGER.info("Calculating hash for TxKey {}", entry.getKey().encodeToBase64());
      affectedContractTransactionHashes.put(
          entry.getKey(), computeAffectedContractTransactionHash(cipherText, entry.getValue()));
    }
    return affectedContractTransactionHashes;
  }

  private byte[] computeAffectedContractTransactionHash(
      byte[] cipherText, EncodedPayload affectedTransaction) {
    MasterKey masterKey = getMasterKey(affectedTransaction);
    return computeCAHash(cipherText, affectedTransaction.getCipherText(), masterKey);
  }

  private byte[] computeCAHash(byte[] c1, byte[] c2, MasterKey masterKey) {
    ByteBuffer byteBuffer =
        ByteBuffer.allocate(c1.length + c2.length + masterKey.getKeyBytes().length);
    byteBuffer.put(c1);
    byteBuffer.put(c2);
    byteBuffer.put(masterKey.getKeyBytes());

    final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
    return digestSHA3.digest(byteBuffer.array());
  }

  @Override
  public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey publicKey) {

    if (payload.getRecipientKeys().isEmpty() || payload.getRecipientBoxes().isEmpty()) {
      throw new RuntimeException("No key or recipient-box to use");
    }

    final MasterKey master =
        this.getMasterKey(
            payload.getRecipientKeys().get(0), payload.getSenderKey(),
            payload.getRecipientNonce(), payload.getRecipientBoxes().get(0).getData());

    final List<byte[]> sealedMasterKeyList =
        this.buildRecipientMasterKeys(
            payload.getSenderKey(), List.of(publicKey), payload.getRecipientNonce(), master);

    return sealedMasterKeyList.get(0);
  }

  @Override
  public EncodedPayload encryptPayload(
      final RawTransaction rawTransaction,
      final List<PublicKey> recipientPublicKeys,
      final PrivacyMetadata privacyMetadata) {

    final MasterKey masterKey =
        this.getMasterKey(
            rawTransaction.getFrom(), rawTransaction.getFrom(),
            rawTransaction.getNonce(), rawTransaction.getEncryptedKey());

    final Nonce recipientNonce = encryptor.randomNonce();

    final List<byte[]> encryptedMasterKeys =
        buildRecipientMasterKeys(
            rawTransaction.getFrom(), recipientPublicKeys, recipientNonce, masterKey);

    final Map<TxHash, byte[]> affectedContractTransactionHashes =
        buildAffectedContractTransactionHashes(
            privacyMetadata.getAffectedContractTransactions().stream()
                .collect(
                    Collectors.toMap(
                        AffectedTransaction::getHash, AffectedTransaction::getPayload)),
            rawTransaction.getEncryptedPayload());

    final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();

    privacyMetadata.getPrivacyGroupId().ifPresent(payloadBuilder::withPrivacyGroupId);

    return payloadBuilder
        .withSenderKey(rawTransaction.getFrom())
        .withCipherText(rawTransaction.getEncryptedPayload())
        .withCipherTextNonce(rawTransaction.getNonce())
        .withRecipientBoxes(encryptedMasterKeys)
        .withRecipientNonce(recipientNonce)
        .withRecipientKeys(recipientPublicKeys)
        .withPrivacyMode(privacyMetadata.getPrivacyMode())
        .withAffectedContractTransactions(affectedContractTransactionHashes)
        .withExecHash(privacyMetadata.getExecHash())
        .withMandatoryRecipients(privacyMetadata.getMandatoryRecipients())
        .build();
  }

  @Override
  public Set<TxHash> findInvalidSecurityHashes(
      EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions) {
    return encodedPayload.getAffectedContractTransactions().entrySet().stream()
        .filter(
            entry -> {
              LOGGER.debug("Verifying hash for TxKey {}", entry.getKey().encodeToBase64());
              TxHash txHash = entry.getKey();

              final Optional<EncodedPayload> affectedTransaction =
                  affectedContractTransactions.stream()
                      .filter(t -> Objects.equals(t.getHash(), txHash))
                      .findFirst()
                      .map(AffectedTransaction::getPayload);
              if (affectedTransaction.isEmpty()) {
                return true;
              }
              byte[] calculatedHash =
                  computeAffectedContractTransactionHash(
                      encodedPayload.getCipherText(), affectedTransaction.get());
              return !Arrays.equals(entry.getValue().getData(), calculatedHash);
            })
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  private List<byte[]> buildRecipientMasterKeys(
      final PublicKey senderPublicKey,
      final List<PublicKey> recipientPublicKeys,
      final Nonce recipientNonce,
      final MasterKey masterKey) {
    final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

    return recipientPublicKeys.stream()
        .map(publicKey -> encryptor.computeSharedKey(publicKey, privateKey))
        .map(
            sharedKey ->
                encryptor.sealAfterPrecomputation(
                    masterKey.getKeyBytes(), recipientNonce, sharedKey))
        .collect(Collectors.toList());
  }

  @Override
  public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
    final MasterKey masterKey = encryptor.createMasterKey();
    final Nonce nonce = encryptor.randomNonce();

    final byte[] cipherText = encryptor.sealAfterPrecomputation(message, nonce, masterKey);

    final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(sender);

    // TODO NL - check if it makes sense to compute a shared key from the public and private parts
    // of the same key
    SharedKey sharedKey = encryptor.computeSharedKey(sender, privateKey);
    final byte[] encryptedMasterKey =
        encryptor.sealAfterPrecomputation(masterKey.getKeyBytes(), nonce, sharedKey);

    return new RawTransaction(cipherText, encryptedMasterKey, nonce, sender);
  }

  @Override
  public byte[] unencryptTransaction(
      final EncodedPayload payload, final PublicKey publicToFindPrivateFor) {
    PublicKey senderPublicKey = payload.getSenderKey();
    final RecipientBox recipientBox;

    // Case 1: PSV transaction, only one box but all recipients known, but our keys come first
    // Case 2: we are a recipient, and only one single box is present, but no recipient key
    // Case 3: we sent the transaction, so all the recipient keys + sender key is present
    // Case 4: a transaction from a very old version of Tessera, where the sender key isn't in the
    // recipient list
    // Case 5: recipients for this node known, along with their boxes

    if (payload.getPrivacyMode() == PrivacyMode.PRIVATE_STATE_VALIDATION) {
      // Case 1
      int index = payload.getRecipientKeys().indexOf(publicToFindPrivateFor);
      if (index == -1) {
        // this key was not listed as a recipient, exit early
        throw new EnclaveException("recipient not found in listed keys");
      }
      recipientBox = payload.getRecipientBoxes().get(index);
    } else if (payload.getRecipientKeys().isEmpty()) {
      // Case 2
      // we are just a standard recipient, so try the only box we have
      // we don't know if it will work, but no other choice
      recipientBox = payload.getRecipientBoxes().get(0);
    } else if (payload.getRecipientKeys().contains(payload.getSenderKey())) {
      // Case 3
      // we are the sender, so any key (incl. the sender) privy should be in the recipient list
      int index = payload.getRecipientKeys().indexOf(publicToFindPrivateFor);
      if (index == -1) {
        // this key was not listed as a recipient, exit early
        throw new EnclaveException("recipient not found in listed keys");
      }
      recipientBox = payload.getRecipientBoxes().get(index);
    } else {
      // Cases 4 and 5
      if (this.getPublicKeys().contains(payload.getSenderKey())) {
        // Case 4

        // all the keys available are the recipients

        // Case 4.1, if the public key to find is the sender, then just pick the first box
        // Case 4.2, otherwise, pick the key and the index

        if (Objects.equals(payload.getSenderKey(), publicToFindPrivateFor)) {
          // Case 4.1
          // we are the sender, so choose some other key as the "sender"
          senderPublicKey = payload.getRecipientKeys().get(0);
          recipientBox = payload.getRecipientBoxes().get(0);
        } else {
          // Case 4.2
          int index = payload.getRecipientKeys().indexOf(publicToFindPrivateFor);
          if (index == -1) {
            // this key was not listed as a recipient, exit early
            throw new EnclaveException("recipient not found in listed keys");
          }
          recipientBox = payload.getRecipientBoxes().get(index);
        }
      } else {
        // Case 5
        int index = payload.getRecipientKeys().indexOf(publicToFindPrivateFor);
        if (index == -1) {
          // this key was not listed as a recipient, exit early
          throw new EnclaveException("recipient not found in listed keys");
        }
        recipientBox = payload.getRecipientBoxes().get(index);
      }
    }

    final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(publicToFindPrivateFor);
    final SharedKey sharedKey = encryptor.computeSharedKey(senderPublicKey, privateKey);

    final Nonce recipientNonce = payload.getRecipientNonce();

    final byte[] masterKeyBytes =
        encryptor.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);

    final MasterKey masterKey = MasterKey.from(masterKeyBytes);

    final byte[] cipherText = payload.getCipherText();
    final Nonce cipherTextNonce = payload.getCipherTextNonce();

    return encryptor.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);
  }

  @Override
  public byte[] unencryptRawPayload(RawTransaction payload) {

    final PrivateKey senderPrivateKey = keyManager.getPrivateKeyForPublicKey(payload.getFrom());

    final SharedKey sharedKey = encryptor.computeSharedKey(payload.getFrom(), senderPrivateKey);

    final byte[] recipientBox = payload.getEncryptedKey();

    final Nonce recipientNonce = payload.getNonce();

    final byte[] masterKeyBytes =
        encryptor.openAfterPrecomputation(recipientBox, recipientNonce, sharedKey);

    final MasterKey masterKey = MasterKey.from(masterKeyBytes);

    final byte[] cipherText = payload.getEncryptedPayload();
    final Nonce cipherTextNonce = payload.getNonce();

    return encryptor.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);
  }

  private MasterKey getMasterKey(
      PublicKey recipient, PublicKey sender, Nonce nonce, byte[] encryptedKey) {

    final SharedKey sharedKey =
        encryptor.computeSharedKey(recipient, keyManager.getPrivateKeyForPublicKey(sender));

    final byte[] masterKeyBytes = encryptor.openAfterPrecomputation(encryptedKey, nonce, sharedKey);

    return MasterKey.from(masterKeyBytes);
  }

  private MasterKey getMasterKey(
      PublicKey recipient, PublicKey sender, Nonce nonce, RecipientBox encryptedKey) {
    return getMasterKey(recipient, sender, nonce, encryptedKey.getData());
  }

  private MasterKey getMasterKey(EncodedPayload encodedPayload) {

    final PublicKey senderPubKey;

    final PublicKey recipientPubKey;

    if (encodedPayload.getRecipientBoxes().isEmpty()) {
      throw new RuntimeException("An EncodedPayload should have at least one recipient box.");
    }

    final RecipientBox recipientBox = encodedPayload.getRecipientBoxes().get(0);

    if (!this.getPublicKeys().contains(encodedPayload.getSenderKey())) {
      // This is a payload originally sent to us by another node
      recipientPubKey = encodedPayload.getSenderKey();
      for (final PublicKey potentialMatchingKey : getPublicKeys()) {
        try {
          return getMasterKey(
              recipientPubKey,
              potentialMatchingKey,
              encodedPayload.getRecipientNonce(),
              recipientBox);
        } catch (EncryptorException ex) {
          LOGGER.debug("Attempted payload decryption using wrong key, discarding.", ex);
        }
      }
      throw new RuntimeException("Unable to decrypt master key");
    }
    // This is a payload that originated from us
    senderPubKey = encodedPayload.getSenderKey();
    recipientPubKey = encodedPayload.getRecipientKeys().get(0);

    return getMasterKey(
        recipientPubKey, senderPubKey, encodedPayload.getRecipientNonce(), recipientBox);
  }

  @Override
  public PublicKey defaultPublicKey() {
    return keyManager.defaultPublicKey();
  }

  @Override
  public Set<PublicKey> getForwardingKeys() {
    return keyManager.getForwardingKeys();
  }

  @Override
  public Set<PublicKey> getPublicKeys() {
    return keyManager.getPublicKeys();
  }

  @Override
  public Status status() {
    return Status.STARTED;
  }
}
