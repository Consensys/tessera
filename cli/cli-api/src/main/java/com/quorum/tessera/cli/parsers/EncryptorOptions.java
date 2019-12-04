package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EncryptorOptions {

    // TODO(cjh) default value using enum instead of hardcoding
    @CommandLine.Option(
        names = {"--encryptor.type"},
        defaultValue = "NACL")
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
