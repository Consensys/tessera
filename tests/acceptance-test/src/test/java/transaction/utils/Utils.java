package transaction.utils;

import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Random;

public class Utils {

  public static byte[] generateTransactionData() {
    Random random = new Random();
    byte[] bytes = new byte[random.nextInt(500) + 1];
    random.nextBytes(bytes);
    return bytes;
  }

  public static PublicKey generateValidButUnknownPublicKey(EncryptorType encryptorType) {
    return getEncryptor(encryptorType).generateNewKeys().getPublicKey();
  }

  public static Encryptor getEncryptor(EncryptorType encryptorType) {
    return getEncryptorFactory(encryptorType).create();
  }

  public static EncryptorFactory getEncryptorFactory(EncryptorType encryptorType) {
    return EncryptorFactory.newFactory(encryptorType.name());
  }
}
