package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.constraints.ValidBase64;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

/**
 * An implementation of ConfigKeyPair for use with remote enclave instances.
 * Nodes are configured only declaring public keys.
 */
public class PublicKeyOnlyKeyPair implements ConfigKeyPair {
    
    @Size(min = 1)
    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String publicKey;

    public PublicKeyOnlyKeyPair(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String getPrivateKey() {
        return null;
    }

    @Override
    public void withPassword(String password) {
    }

    @Override
    public String getPassword() {
        return null;
    }

}
