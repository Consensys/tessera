package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidKeyDataConfig;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.nacl.NaclException;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;

public class InlineKeypair implements ConfigKeyPair {

    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String publicKey;

    @NotNull
    @ValidKeyDataConfig
    @XmlElement(name = "config")
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
            try {
                return KeyEncryptorFactory.create().decryptPrivateKey(pkd, password).toString();
            } catch (final NaclException ex) {
                return "NACL_FAILURE";
            }
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
