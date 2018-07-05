package com.github.nexus.config.adapters;

import com.github.nexus.argon2.ArgonOptions;
import com.github.nexus.config.KeyData;
import com.github.nexus.config.keys.KeyConfig;
import com.github.nexus.config.keys.KeyEncryptor;
import com.github.nexus.config.keys.KeyEncryptorFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import static com.github.nexus.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyDataAdapter extends XmlAdapter<KeyData, KeyData> {

    @Override
    public KeyData unmarshal(final KeyData keyData) {

        if (keyData.hasKeys()) {
            return keyData;
        }

        if (keyData.getConfig().getType() == UNLOCKED) {
            return new KeyData(keyData.getConfig(), keyData.getConfig().getValue(), keyData.getPublicKey());
        }

        final KeyEncryptor kg = KeyEncryptorFactory.create();

        //need to decrypt
        return new KeyData(
            keyData.getConfig(),
            kg.decryptPrivateKey(
                KeyConfig.Builder.create()
                    .password(keyData.getConfig().getPassword())
                    .asalt(keyData.getConfig().getAsalt().getBytes(UTF_8))
                    .sbox(keyData.getConfig().getSbox().getBytes(UTF_8))
                    .snonce(keyData.getConfig().getSnonce().getBytes(UTF_8))
                    .argonOptions(
                        new ArgonOptions(
                            keyData.getConfig().getArgonOptions().getAlgorithm(),
                            keyData.getConfig().getArgonOptions().getIterations(),
                            keyData.getConfig().getArgonOptions().getMemory(),
                            keyData.getConfig().getArgonOptions().getParallelism()
                        )
                    )
                    .build()
            ).toString(),
            keyData.getPublicKey()
        );
    }

    @Override
    public KeyData marshal(final KeyData keyData) {

        if (keyData.getConfig().getType() != UNLOCKED) {
            return new KeyData(keyData.getConfig(), null, keyData.getPublicKey());
        }

        return keyData;
    }

}
