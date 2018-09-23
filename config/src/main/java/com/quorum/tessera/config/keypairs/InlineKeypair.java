package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;

public class InlineKeypair implements ConfigKeyPair {

    private final String publicKey;

    private final KeyDataConfig privateKeyConfig;

    private String password = "";

    public InlineKeypair(final String publicKey, final KeyDataConfig privateKeyConfig) {
        this.publicKey = publicKey;
        this.privateKeyConfig = privateKeyConfig;
    }

    @Override
    public KeyData marshal() {
        return new KeyData(privateKeyConfig, null, publicKey, null, null);
    }

    public KeyDataConfig getPrivateKeyConfig() {
        return this.privateKeyConfig;
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String getPrivateKey() {
        final PrivateKeyData pkd = privateKeyConfig.getPrivateKeyData();

        if (privateKeyConfig.getType() == UNLOCKED) {
            return privateKeyConfig.getValue();
        } else {
            return KeyEncryptorFactory.create().decryptPrivateKey(pkd, password).toString();
        }
    }

    @Override
    public void withPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
