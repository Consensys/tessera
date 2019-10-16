package com.quorum.tessera.encryption;

public class MockEncryptorFactory implements EncryptorFactory {

    @Override
    public Encryptor create() {
        return MockEncryptor.INSTANCE;
    }
    
}
