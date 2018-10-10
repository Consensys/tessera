package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidInlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.nacl.NaclException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import com.quorum.tessera.encryption.PrivateKey;

@ValidInlineKeypair
public class InlineKeypair implements ConfigKeyPair {

    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String publicKey;

    @NotNull
    @XmlElement(name = "config")
    private final KeyDataConfig privateKeyConfig;

    private String password = "";

    public InlineKeypair(final String publicKey, final KeyDataConfig privateKeyConfig) {
        this.publicKey = publicKey;
        this.privateKeyConfig = privateKeyConfig;
    }

    public KeyDataConfig getPrivateKeyConfig() {
        return this.privateKeyConfig;
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    @Pattern(regexp = "^((?!NACL_FAILURE).)*$", message = "Could not decrypt the private key with the provided password, please double check the passwords provided")
    public String getPrivateKey() {
        final PrivateKeyData pkd = privateKeyConfig.getPrivateKeyData();

        if (privateKeyConfig.getType() == UNLOCKED) {
            return privateKeyConfig.getValue();
        } else {
            try {
                PrivateKey privateKey = KeyEncryptorFactory.create().decryptPrivateKey(pkd, password);
                return privateKey.encodeToBase64();
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
