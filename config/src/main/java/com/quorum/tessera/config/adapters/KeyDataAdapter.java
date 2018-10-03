package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.*;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Objects;

public class KeyDataAdapter extends XmlAdapter<KeyData, ConfigKeyPair> {

    public static final String NACL_FAILURE_TOKEN = "NACL_FAILURE";
    
    @Override
    public ConfigKeyPair unmarshal(final KeyData keyData) {

        //case 1, the keys are provided inline
        if (Objects.nonNull(keyData.getPrivateKey()) && Objects.nonNull(keyData.getPublicKey())) {
            return new DirectKeyPair(keyData.getPublicKey(), keyData.getPrivateKey());
        }

        //case 2, the config is provided inline
        if (keyData.getPublicKey() != null && keyData.getConfig() != null) {
            return new InlineKeypair(keyData.getPublicKey(), keyData.getConfig());
        }

        //case 3, the keys are provided inside a file
        if(keyData.getPublicKeyPath() != null && keyData.getPrivateKeyPath() != null) {
            return new FilesystemKeyPair(keyData.getPublicKeyPath(), keyData.getPrivateKeyPath());
        }

        //case 4, the key config specified is invalid
        return new UnsupportedKeyPair(
            keyData.getConfig(),
            keyData.getPrivateKey(),
            keyData.getPublicKey(),
            keyData.getPrivateKeyPath(),
            keyData.getPublicKeyPath()
        );
    }

    @Override
    public KeyData marshal(final ConfigKeyPair keyData) {

        if(keyData instanceof DirectKeyPair) {
            DirectKeyPair kp = (DirectKeyPair) keyData;
            return new KeyData(null, kp.getPrivateKey(), kp.getPublicKey(), null, null);
        }

        if(keyData instanceof InlineKeypair) {
            InlineKeypair kp = (InlineKeypair) keyData;
            return new KeyData(kp.getPrivateKeyConfig(), null, kp.getPublicKey(), null, null);
        }

        if(keyData instanceof FilesystemKeyPair) {
            FilesystemKeyPair kp = (FilesystemKeyPair) keyData;
            return new KeyData(null, null, null, kp.getPrivateKeyPath(), kp.getPublicKeyPath());
        }

        if(keyData instanceof UnsupportedKeyPair) {
            UnsupportedKeyPair kp = (UnsupportedKeyPair) keyData;
            return new KeyData(kp.getConfig(), kp.getPrivateKey(), kp.getPublicKey(), kp.getPrivateKeyPath(), kp.getPublicKeyPath());
        }

        throw new UnsupportedOperationException("The keypair type " + keyData.getClass() + " is not allowed");
    }
}
