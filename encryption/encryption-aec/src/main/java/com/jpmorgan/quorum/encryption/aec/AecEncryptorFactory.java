package com.jpmorgan.quorum.encryption.aec;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import java.util.Map;

public class AecEncryptorFactory implements EncryptorFactory {

    @Override
    public String getType() {
        return "AEC";
    }

    @Override
    public Encryptor create(Map<String, String> properties) {
        return new AecEncryptor("AES/GCM/NoPadding", "secp256r1", 24, 32);
    }
}
