package net.consensys.tessera.migration.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import net.consensys.tessera.migration.OrionKeyHelper;

import java.util.Base64;
import java.util.List;

public interface TesseraEnclaveFactory {

    static Enclave createEnclave(OrionKeyHelper orionKeyHelper) {
        Config tesseraConfig = new Config();
        EncryptorConfig tesseraEncryptorConfig = new EncryptorConfig();
        tesseraEncryptorConfig.setType(EncryptorType.NACL);

        tesseraConfig.setKeys(new KeyConfiguration());

        KeyData keyData = orionKeyHelper.getKeyPairs().stream().map(p -> {
            KeyData keyData1 = new KeyData();
            keyData1.setPrivateKey(Base64.getEncoder().encodeToString(p.secretKey().bytesArray()));
            keyData1.setPublicKey(Base64.getEncoder().encodeToString(p.publicKey().bytesArray()));
            return keyData1;
        }).findFirst().get();

        tesseraConfig.getKeys().setKeyData(List.of(keyData));
        tesseraConfig.setEncryptor(tesseraEncryptorConfig);

        return EnclaveFactory.create().create(tesseraConfig);
    }
}
