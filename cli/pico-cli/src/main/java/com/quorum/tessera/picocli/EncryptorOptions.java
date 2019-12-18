package com.quorum.tessera.picocli;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class EncryptorOptions {

    @CommandLine.Option(
            names = {"--encryptor.type"},
            description = "Valid values: ${COMPLETION-CANDIDATES}")
    EncryptorType type;

    @CommandLine.Option(names = {"--encryptor.symmetricCipher"})
    String symmetricCipher;

    @CommandLine.Option(names = {"--encryptor.ellipticCurve"})
    String ellipticCurve;

    @CommandLine.Option(names = {"--encryptor.nonceLength"})
    String nonceLength;

    @CommandLine.Option(names = {"--encryptor.sharedKeyLength"})
    String sharedKeyLength;

    EncryptorConfig parseEncryptorConfig() {
        final EncryptorConfig encryptorConfig = new EncryptorConfig();

        // we set the default here instead of in the option annotation as enum values cannot be used in annotations
        if (Objects.isNull(type)) {
            type = EncryptorType.NACL;
        }

        Map<String, String> properties = new HashMap<>();
        if (type == EncryptorType.EC) {

            Optional.ofNullable(symmetricCipher).ifPresent(v -> properties.put("symmetricCipher", v));
            Optional.ofNullable(ellipticCurve).ifPresent(v -> properties.put("ellipticCurve", v));
            Optional.ofNullable(nonceLength).ifPresent(v -> properties.put("nonceLength", v));
            Optional.ofNullable(sharedKeyLength).ifPresent(v -> properties.put("sharedKeyLength", v));
        }

        encryptorConfig.setType(type);
        encryptorConfig.setProperties(properties);

        return encryptorConfig;
    }
}
