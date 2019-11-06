package com.quorum.tessera.config;

import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;

import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.util.Optional;

public class JaxbConfigFactory implements ConfigFactory {

    private final KeyEncryptorFactory keyEncryptorFactory;

    protected JaxbConfigFactory(KeyEncryptorFactory keyEncryptorFactory) {
        this.keyEncryptorFactory = keyEncryptorFactory;
    }

    public JaxbConfigFactory() {
        this(KeyEncryptorFactory.newFactory());
    }

    private static final EncryptorConfig DEFAULT_ENCRYPTOR_CONFIG =
            new EncryptorConfig() {
                {
                    setType(EncryptorType.NACL);
                }
            };

    @Override
    public Config create(final InputStream configData) {

        byte[] originalData =
                Stream.of(configData)
                        .map(InputStreamReader::new)
                        .map(BufferedReader::new)
                        .flatMap(BufferedReader::lines)
                        .collect(Collectors.joining(System.lineSeparator()))
                        .getBytes();

        final Config initialConfig = JaxbUtil.unmarshal(new ByteArrayInputStream(originalData), Config.class);

        EncryptorConfig encryptorConfig =
                Optional.ofNullable(initialConfig.getEncryptor()).orElse(DEFAULT_ENCRYPTOR_CONFIG);
        // Initialise the key encrypter it will store into holder object.
        keyEncryptorFactory.create(encryptorConfig);

        final Config config = JaxbUtil.unmarshal(new ByteArrayInputStream(originalData), Config.class);
        config.setEncryptor(encryptorConfig);

        return config;
    }
}
