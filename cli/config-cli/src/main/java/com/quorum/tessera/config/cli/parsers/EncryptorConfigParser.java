package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.parsers.Parser;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;

public class EncryptorConfigParser implements Parser<EncryptorConfig> {

    protected static final String NO_ENCRYPTOR_DEFINED_ERROR_MESSAGE =
            "Encryptor type hasn't been defined in the config file or as a cli arg";

    private final FilesDelegate filesDelegate;

    public EncryptorConfigParser() {
        this(FilesDelegate.create());
    }

    protected EncryptorConfigParser(FilesDelegate filesDelegate) {
        this.filesDelegate = Objects.requireNonNull(filesDelegate);
    }

    private static final EncryptorType DEFAULT_ENC_TYPE = EncryptorType.NACL;

    @Override
    public EncryptorConfig parse(CommandLine commandLine) throws IOException {

        final String encryptorTypeValue = commandLine.getOptionValue("encryptor.type");

        if (commandLine.hasOption("configfile")) {
            final String path = commandLine.getOptionValue("configfile");
            final Config config = JaxbUtil.unmarshal(filesDelegate.newInputStream(Paths.get(path)), Config.class);

            if (Objects.nonNull(config.getEncryptor())) {
                return config.getEncryptor();
            } else if (commandLine.hasOption("keygen")) {
                if (Objects.isNull(encryptorTypeValue)) {
                    throw new CliException(NO_ENCRYPTOR_DEFINED_ERROR_MESSAGE);
                }
            }
        }

        final EncryptorConfig encryptorConfig = new EncryptorConfig();

        final EncryptorType encryptorType =
                Optional.ofNullable(encryptorTypeValue)
                        .map(String::toUpperCase)
                        .map(EncryptorType::valueOf)
                        .orElse(DEFAULT_ENC_TYPE);

        encryptorConfig.setType(encryptorType);

        Map<String, String> properties = new HashMap<>();
        if (encryptorType == EncryptorType.EC) {

            Optional.ofNullable(commandLine.getOptionValue("encryptor.symmetricCipher"))
                    .ifPresent(v -> properties.put("symmetricCipher", v));

            Optional.ofNullable(commandLine.getOptionValue("encryptor.ellipticCurve"))
                    .ifPresent(v -> properties.put("ellipticCurve", v));

            Optional.ofNullable(commandLine.getOptionValue("encryptor.nonceLength"))
                    .ifPresent(v -> properties.put("nonceLength", v));

            Optional.ofNullable(commandLine.getOptionValue("encryptor.sharedKeyLength"))
                    .ifPresent(v -> properties.put("sharedKeyLength", v));
        }

        encryptorConfig.setProperties(properties);

        return encryptorConfig;
    }
}
