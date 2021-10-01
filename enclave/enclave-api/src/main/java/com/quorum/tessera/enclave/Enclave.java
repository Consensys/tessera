package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * An {@link Enclave} provides encryption/decryption functions and keeps hold of all the nodes
 * private keys so the do not leak into other services.
 */
public interface Enclave extends Service {

  /**
   * Retrieves the public key to use if no key is specified for an operation There is no guarantee
   * this key remains the same between runs of the Enclave.
   *
   * @return the public key that has been assigned as the default
   */
  PublicKey defaultPublicKey();

  /**
   * Returns a set of public keys that should be included as recipients to all transactions produced
   * by this node. The keys are not be managed by this node.
   *
   * @return the set of public keys to be added to transactions
   */
  Set<PublicKey> getForwardingKeys();

  /**
   * Returns all the public keys that are managed by this Enclave.
   *
   * @return all public keys managed by this Enclave
   */
  Set<PublicKey> getPublicKeys();

  /**
   * Encrypts a message using the specified sender and a list of recipients. Returns a {@link
   * EncodedPayload} which contains all the encrypted information, including the recipients and
   * their encrypted master keys.
   *
   * @param message the message to be encrypted
   * @param senderPublicKey the public key which this enclave manages
   * @param recipientPublicKeys the recipients to encrypt this message for
   * @param privacyMetadata privacy metadata of the transaction
   * @return the encrypted information, represented by an {@link EncodedPayload}
   */
  EncodedPayload encryptPayload(
      byte[] message,
      PublicKey senderPublicKey,
      List<PublicKey> recipientPublicKeys,
      PrivacyMetadata privacyMetadata);

  /**
   * Decrypts a {@link RawTransaction} so that it can be re-encrypted into a {@link EncodedPayload}
   * with the given recipient list
   *
   * @param rawTransaction the transaction to decrypt and re-encrypt with recipients
   * @param recipientPublicKeys the recipients to encrypt the transaction for
   * @param privacyMetadata privacy metadata of the transaction
   * @return the encrypted information, represented by an {@link EncodedPayload}
   */
  EncodedPayload encryptPayload(
      RawTransaction rawTransaction,
      List<PublicKey> recipientPublicKeys,
      PrivacyMetadata privacyMetadata);

  /**
   * Filters the affectedContractTransaction hashes by removing those that do not pass the security
   * hash validation
   *
   * @param encodedPayload the encoded payload to check
   * @param affectedContractTransactions the map of tx hash to encoded payloads
   * @return the map of filtered affectedContractTransaction security hashes
   */
  Set<TxHash> findInvalidSecurityHashes(
      EncodedPayload encodedPayload, List<AffectedTransaction> affectedContractTransactions);

  /**
   * Encrypt a payload without any recipients that can be retrieved later. The payload is encrypted
   * using the private key that is related to the given public key.
   *
   * @param message the message to be encrypted
   * @param sender the sender's public key to encrypt the transaction with
   * @return the encrypted transaction
   */
  RawTransaction encryptRawPayload(byte[] message, PublicKey sender);

  /**
   * Decrypt a transaction and fetch the original message using the given public key. Throws an
   * {@link com.quorum.tessera.encryption.EncryptorException} if the provided public key OR one of
   * the Enclave's managed keys cannot be used to decrypt the payload
   *
   * @param payload the encrypted payload
   * @param providedKey the key to use for decryption, if the payload wasn't sent by this Enclave
   * @return the original, decrypted message
   */
  byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey);

  /**
   * Decrypt a raw payload and fetch the original message. Throws an {@link
   * com.quorum.tessera.encryption.EncryptorException} if the provided public key OR one of the
   * Enclave's managed keys cannot be used to decrypt the payload
   *
   * @param payload the encrypted raw payload
   * @return the original, decrypted message
   */
  byte[] unencryptRawPayload(RawTransaction payload);

  /**
   * Creates a new recipient box for the payload, for which we must be the originator. At least one
   * recipient must already be available to be able to decrypt the master key.
   *
   * @param payload the payload to add a recipient to
   * @param recipientKey the new recipient key to add
   */
  byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey);

  @Override
  default void start() {}

  @Override
  default void stop() {}

  static Enclave create() {
    return ServiceLoader.load(Enclave.class).findFirst().get();
  }
}
