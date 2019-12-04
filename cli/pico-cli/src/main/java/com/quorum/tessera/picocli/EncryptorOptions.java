package com.quorum.tessera.picocli;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EncryptorOptions {

    @CommandLine.Option(
            names = {"--encryptor.type"},
            description = "Valid values: ${COMPLETION-CANDIDATES}")
    public EncryptorType type;

    @CommandLine.Option(names = {"--encryptor.symmetricCipher"})
    public String symmetricCipher;

    @CommandLine.Option(names = {"--encryptor.ellipticCurve"})
    public String ellipticCurve;

    @CommandLine.Option(names = {"--encryptor.nonceLength"})
    public String nonceLength;

    @CommandLine.Option(names = {"--encryptor.sharedKeyLength"})
    public String sharedKeyLength;

    public EncryptorConfig parseEncryptorConfig() {
        final EncryptorConfig encryptorConfig = new EncryptorConfig();

        // we set the default here instead of in the option annotation as enum values cannot be used in annotationss
        if (Objects.isNull(type)) {
            type = EncryptorType.NACL;
        }

        encryptorConfig.setType(type);

        if (type == EncryptorType.EC) {
            Map<String, String> properties = new HashMap<>();

            Optional.ofNullable(symmetricCipher).ifPresent(v -> properties.put("symmetricCipher", v));
            Optional.ofNullable(ellipticCurve).ifPresent(v -> properties.put("ellipticCurve", v));
            Optional.ofNullable(nonceLength).ifPresent(v -> properties.put("nonceLength", v));
            Optional.ofNullable(sharedKeyLength).ifPresent(v -> properties.put("sharedKeyLength", v));

            encryptorConfig.setProperties(properties);
        }

        return encryptorConfig;
    }
}
