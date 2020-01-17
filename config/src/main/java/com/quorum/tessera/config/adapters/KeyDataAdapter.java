package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptorHolder;
import com.quorum.tessera.config.util.KeyDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class KeyDataAdapter extends XmlAdapter<KeyData, ConfigKeyPair> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDataAdapter.class);

    public static final String NACL_FAILURE_TOKEN = "NACL_FAILURE";

    private KeyEncryptorHolder keyEncryptorHolder = KeyEncryptorHolder.INSTANCE;

    @Override
    public ConfigKeyPair unmarshal(final KeyData keyData) {

        if (!keyEncryptorHolder.getKeyEncryptor().isPresent()) {
            LOGGER.debug("Ignoring  unmarshal as we pending keyEncryptor initialisation");
            return null;
        }

        return KeyDataUtil.unmarshal(keyData,keyEncryptorHolder.getKeyEncryptor().get());

    }

    @Override
    public KeyData marshal(final ConfigKeyPair keyPair) {

        if (!keyEncryptorHolder.getKeyEncryptor().isPresent()) {
            return null;
        }

        return KeyDataUtil.marshal(keyPair);

    }
}
