package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.*;
import java.util.Base64;
import java.util.Optional;
import javax.json.JsonObject;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptorHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptorHelper.class);

  private final Encryptor encryptor;

  public EncryptorHelper(Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  public boolean canDecrypt(
      JsonObject transaction,
      JsonObject encryptedKey,
      final PublicKey publicKey,
      final PrivateKey ourPrivateKey) {

    final SharedKey sharedKey = encryptor.computeSharedKey(publicKey, ourPrivateKey);

    final Nonce nonce =
        Optional.of(transaction).map(j -> j.getString("nonce").getBytes()).map(Nonce::new).get();

    final byte[] decryptedKeyData;
    try {
      byte[] encryptedKeyData =
          Optional.of(encryptedKey)
              .map(j -> j.getString("encoded"))
              .map(Base64.getDecoder()::decode)
              .get();

      decryptedKeyData = encryptor.openAfterPrecomputation(encryptedKeyData, nonce, sharedKey);
    } catch (EncryptorException e) {
      LOGGER.error(null, e);
      // Wrong key, keep trying the others.
      return false;
    }

    final MasterKey masterKey = MasterKey.from(decryptedKeyData);

    // this isn't used anywhere, but acts as a sanity check we got all the keys right.
    // TODO: this should not fail, but if it does, do we want to catch the exception or let it blow
    // up?

    byte[] cipherText = transaction.getString("cipherText").getBytes();

    encryptor.openAfterPrecomputation(cipherText, new Nonce(new byte[24]), masterKey);

    return true;
  }

  public boolean canDecrypt(
      final EncryptedPayload transaction,
      final EncryptedKey encryptedKey,
      final PublicKey publicKey,
      final PrivateKey ourPrivateKey) {

    // "publicKey" may either be the recipient public key (if we were the sender),
    // or the tx sender public key (if we were a recipient)

    LOGGER.info(
        "Create sharedKey from {} and {}",
        publicKey.encodeToBase64(),
        ourPrivateKey.encodeToBase64());

    final SharedKey sharedKey = encryptor.computeSharedKey(publicKey, ourPrivateKey);

    final Nonce nonce = new Nonce(transaction.nonce());

    final byte[] decryptedKeyData;
    try {
      decryptedKeyData =
          encryptor.openAfterPrecomputation(encryptedKey.getEncoded(), nonce, sharedKey);
    } catch (EncryptorException e) {
      LOGGER.error(null, e);
      // Wrong key, keep trying the others.
      return false;
    }

    final MasterKey masterKey = MasterKey.from(decryptedKeyData);

    // this isn't used anywhere, but acts as a sanity check we got all the keys right.
    // TODO: this should not fail, but if it does, do we want to catch the exception or let it blow
    // up?
    encryptor.openAfterPrecomputation(transaction.cipherText(), new Nonce(new byte[24]), masterKey);

    return true;
  }
}
