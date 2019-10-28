package com.quorum.tessera.encryption;

import java.util.Map;

public class MockEncryptorFactory implements EncryptorFactory {

    @Override
    public Encryptor create(Map<String, String> properties) {
        return MockEncryptor.INSTANCE;
    }

    @Override
    public String getType() {
        return "MOCK";
    }
}
