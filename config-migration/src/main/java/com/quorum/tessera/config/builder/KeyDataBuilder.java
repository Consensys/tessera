package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class KeyDataBuilder {

    private KeyDataBuilder() {
    }

    public static KeyDataBuilder create() {
        return new KeyDataBuilder();
    }

    private List<String> publicKeys = Collections.emptyList();

    private List<String> privateKeys = Collections.emptyList();

    private Path privateKeyPasswordFile;

    public KeyDataBuilder withPublicKeys(final List<String> publicKeys) {
        this.publicKeys = publicKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeys(final List<String> privateKeys) {
        this.privateKeys = privateKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeyPasswordFile(final Path privateKeyPasswordFile) {
        this.privateKeyPasswordFile = privateKeyPasswordFile;
        return this;
    }

    public KeyConfiguration build() {

        if(publicKeys.size() != privateKeys.size()) {
            throw new ConfigException(new RuntimeException("Different amount of public and private keys supplied"));
        }

        final List<KeyData> keyData = IntStream
            .range(0, publicKeys.size())
            .mapToObj(i -> new KeyData(null, null, null, Paths.get(privateKeys.get(i)), Paths.get(publicKeys.get(i))))
            .collect(toList());

        return new KeyConfiguration(privateKeyPasswordFile, Collections.emptyList(), keyData);
    }

}
