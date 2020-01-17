package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;

import java.util.List;

public class RuntimeContextBuilder {

    private Encryptor encryptor;

    public RuntimeContextBuilder encryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
        return this;
    }

    public RuntimeContext build() {
        return new RuntimeContext() {
            @Override
            public List<KeyPair> getKeys() {
                return null;
            }

            @Override
            public KeyEncryptor getKeyEncryptor() {
                return null;
            }
        };

    }



}
