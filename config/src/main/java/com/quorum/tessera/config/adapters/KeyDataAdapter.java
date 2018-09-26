package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;

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
        return new FilesystemKeyPair(keyData.getPublicKeyPath(), keyData.getPrivateKeyPath());

        //TODO Add keyvault case
        //TODO Do we need negative case that (i.e. the case that was previously handled by returning the same keydata object so that validation would pick up any errors?
    }

    @Override
    public KeyData marshal(final ConfigKeyPair keyData) {
        return keyData.marshal();
    }

}
