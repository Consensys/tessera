package com.github.nexus.config.adapters;

import com.github.nexus.config.KeyData;
import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.KeyDataConfigStore;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class KeyDataAdapter extends XmlAdapter<KeyData, KeyData> {

    @Override
    public KeyData unmarshal(KeyData keyData) throws Exception {

        if (keyData.hasKeys()) {
            return keyData;
        }

        //TODO: Generate key pairs from and create populated keyData
        KeyDataConfig keyDataConfig = KeyDataConfigStore.INSTANCE.pop();

        return new KeyData(keyDataConfig, null, null);
    }

    @Override
    public KeyData marshal(KeyData keyData) throws Exception {
        return keyData;
    }

}
