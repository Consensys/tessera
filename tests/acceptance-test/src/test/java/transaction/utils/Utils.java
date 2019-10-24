package transaction.utils;

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

    public static PublicKey generateValidButUnknownPublicKey() {
        return EncryptorFactory.newFactory().create().generateNewKeys().getPublicKey();
    }
}
