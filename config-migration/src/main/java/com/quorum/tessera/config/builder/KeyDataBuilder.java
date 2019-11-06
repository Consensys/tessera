package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class KeyDataBuilder {

    private KeyDataBuilder() {}

    public static KeyDataBuilder create() {
        return new KeyDataBuilder();
    }

    private List<String> publicKeys = Collections.emptyList();

    private List<String> privateKeys = Collections.emptyList();

    private String privateKeyPasswordFile;

    private String workdir;

    public KeyDataBuilder withPublicKeys(final List<String> publicKeys) {
        this.publicKeys = publicKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeys(final List<String> privateKeys) {
        this.privateKeys = privateKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeyPasswordFile(final String privateKeyPasswordFile) {
        this.privateKeyPasswordFile = privateKeyPasswordFile;
        return this;
    }

    public KeyDataBuilder withWorkingDirectory(final String workdir) {
        this.workdir = workdir;
        return this;
    }

    public KeyConfiguration build() {
        if (publicKeys.size() != privateKeys.size()) {
            throw new ConfigException(new RuntimeException("Different amount of public and private keys supplied"));
        }

        Map<Path, Path> mappedKeyPairs =
                IntStream.range(0, publicKeys.size())
                        .boxed()
                        .collect(
                                Collectors.toMap(
                                        i -> ConfigBuilder.toPath(workdir, publicKeys.get(i)),
                                        i -> ConfigBuilder.toPath(workdir, privateKeys.get(i))));

        KeyEncryptor keyEncryptor =
                KeyEncryptorFactory.newFactory()
                        .create(
                                new EncryptorConfig() {
                                    {
                                        setType(EncryptorType.NACL);
                                    }
                                });
        KeyDataConfig keyDataConfig =
                new KeyDataConfig(
                        new PrivateKeyData() {
                            {
                                setValue("PASSWORD");
                            }
                        },
                        PrivateKeyType.UNLOCKED);

        InlineKeypair inlineKeypair = new InlineKeypair(workdir, keyDataConfig, keyEncryptor);

        final List<ConfigKeyPair> keyData =
                mappedKeyPairs.entrySet().stream()
                        .map(pair -> new FilesystemKeyPair(pair.getKey(), pair.getValue(), inlineKeypair))
                        .collect(toList());

        final Path privateKeyPasswordFilePath;
        if (!Objects.isNull(workdir) && !Objects.isNull(privateKeyPasswordFile)) {
            privateKeyPasswordFilePath = Paths.get(workdir, privateKeyPasswordFile);
        } else if (!Objects.isNull(privateKeyPasswordFile)) {
            privateKeyPasswordFilePath = Paths.get(privateKeyPasswordFile);
        } else {
            privateKeyPasswordFilePath = null;
        }

        return new KeyConfiguration(privateKeyPasswordFilePath, null, keyData, null, null);
    }
}
