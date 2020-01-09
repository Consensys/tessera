package com.quorum.tessera.config.keys;

import java.util.Optional;

public enum KeyEncryptorHolder {
    INSTANCE;

    private KeyEncryptor keyEncryptor;

    public Optional<KeyEncryptor> getKeyEncryptor() {
        return Optional.ofNullable(keyEncryptor);
    }

    public void setKeyEncryptor(KeyEncryptor keyEncryptor) {
        this.keyEncryptor = keyEncryptor;
    }
}
