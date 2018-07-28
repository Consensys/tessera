package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.IOCallback;
import com.quorum.tessera.config.util.JaxbUtil;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyDataAdapter extends XmlAdapter<KeyData, KeyData> {

    @Override
    public KeyData unmarshal(final KeyData keyData) {

        //case 1, the keys are provided inline
        if (keyData.hasKeys()) {
            return keyData;
        }

        //case 2, the config is provided inline
        if (keyData.getPublicKeyPath() == null && keyData.getPrivateKeyPath() == null) {
            return unmarshalInline(keyData);
        }

        if (keyData.getPublicKeyPath() == null || keyData.getPrivateKeyPath() == null) {
            throw new IllegalArgumentException("When providing key paths, must give both public and private");
        }

        //case 3, the keys are provided inside a file
        return unmarshalFile(keyData.getPublicKeyPath(), keyData.getPrivateKeyPath());
    }

    private KeyData unmarshalFile(final Path publicKeyPath, final Path privateKeyPath) {
        final byte[] publicKey = IOCallback.execute(() -> Files.readAllBytes(publicKeyPath));
        final String publicKeyString = new String(publicKey, UTF_8);

        final byte[] privateKey = IOCallback.execute(() -> Files.readAllBytes(privateKeyPath));
        final String privateKeyString = new String(privateKey, UTF_8);

        return this.unmarshalInline(
            new KeyData(
                JaxbUtil.unmarshal(new ByteArrayInputStream(privateKeyString.getBytes(UTF_8)), KeyDataConfig.class),
                null,
                publicKeyString,
                privateKeyPath,
                publicKeyPath
            )
        );

    }

    private KeyData unmarshalInline(final KeyData keyData) {
        if (keyData.getConfig().getType() == PrivateKeyType.UNLOCKED) {
            return new KeyData(keyData.getConfig(), keyData.getConfig().getValue(), keyData.getPublicKey(), null, null);
        }

        final KeyEncryptor kg = KeyEncryptorFactory.create();

        //need to decrypt
        return new KeyData(
            keyData.getConfig(),
            kg.decryptPrivateKey(keyData.getConfig()).toString(),
            keyData.getPublicKey(),
            keyData.getPrivateKeyPath(),
            keyData.getPublicKeyPath()
        );
    }

    @Override
    public KeyData marshal(final KeyData keyData) {

        if (keyData.getConfig().getType() != PrivateKeyType.UNLOCKED) {
            return new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(
                        keyData.getConfig().getPrivateKeyData().getValue(),
                        keyData.getConfig().getPrivateKeyData().getSnonce(),
                        keyData.getConfig().getPrivateKeyData().getAsalt(),
                        keyData.getConfig().getPrivateKeyData().getSbox(),
                        keyData.getConfig().getPrivateKeyData().getArgonOptions(),
                        null
                    ),
                    keyData.getConfig().getType()
                ),
                null,
                keyData.getPublicKey(),
                null,
                null
            );
        }

        return keyData;
    }

}
