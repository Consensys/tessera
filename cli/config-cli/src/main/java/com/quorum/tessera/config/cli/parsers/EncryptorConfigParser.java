package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.parsers.Parser;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;

public class EncryptorConfigParser implements Parser<EncryptorConfig> {

    @Override
    public EncryptorConfig parse(CommandLine commandLine) throws IOException {

        if (commandLine.hasOption("configfile")) {
            final String path = commandLine.getOptionValue("configfile");
            final Config config = JaxbUtil.unmarshal(Files.newInputStream(Paths.get(path)), Config.class);

            if (config.getEncryptor() != null) {
                return config.getEncryptor();
            }
        }

        if (!commandLine.hasOption("encryptor.type")) {
            throw new CliException("Encryptor type hasn't been defined in the config file or as a cli arg");
        }

        EncryptorConfig encryptorConfig = new EncryptorConfig();

        EncryptorType encryptorType = EncryptorType.valueOf(commandLine.getOptionValue("encryptor.type").toUpperCase());
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
