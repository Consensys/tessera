package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeysConverter;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.config.util.PasswordReader;
import com.quorum.tessera.nacl.Key;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class KeyUpdateParser implements Parser<Optional> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUpdateParser.class);

    private final KeyEncryptor keyEncryptor;

    private final PasswordReader passwordReader;

    public KeyUpdateParser(final KeyEncryptor keyEncryptor, final PasswordReader passwordReader) {
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
        this.passwordReader = Objects.requireNonNull(passwordReader);
    }

    @Override
    public Optional parse(final CommandLine commandLine) throws IOException {
        if (commandLine.hasOption("updatepassword")) {
            final ArgonOptions argonOptions = argonOptions(commandLine);
            final List<String> passwords = passwords(commandLine);
            final Path keypath = privateKeyPath(commandLine);

            final KeyDataConfig keyDataConfig = JaxbUtil.unmarshal(Files.newInputStream(keypath), KeyDataConfig.class);
            final Key privateKey = this.getExistingKey(keyDataConfig, passwords);

            final String newPassword = passwordReader.requestUserPassword();

            final KeyDataConfig updatedKey;
            if(newPassword.isEmpty()) {
                final PrivateKeyData privateKeyData = new PrivateKeyData(privateKey.toString(), null, null, null, null, null);
                updatedKey = new KeyDataConfig(privateKeyData, PrivateKeyType.UNLOCKED);
            } else {
                final PrivateKeyData privateKeyData = keyEncryptor.encryptPrivateKey(privateKey, newPassword, argonOptions);
                updatedKey = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
            }

            //write the key to file
            Files.write(keypath, JaxbUtil.marshalToString(updatedKey).getBytes(UTF_8));
            System.out.println("Private key at " + keypath.toString() + " updated.");
        }

        return Optional.empty();
    }

    Key getExistingKey(final KeyDataConfig kdc, final List<String> passwords) {

        if (kdc.getType() == PrivateKeyType.UNLOCKED) {
            return KeysConverter.convert(singletonList(kdc.getValue())).get(0);
        } else {

            for (final String pass : passwords) {
                try {
                    return keyEncryptor.decryptPrivateKey(kdc.getPrivateKeyData(), pass);
                } catch (final Exception e) {
                    LOGGER.debug("Password failed to decrypt. Trying next if available.");
                }
            }

            throw new IllegalArgumentException("Locked key but no valid password given");
        }
    }

    static Path privateKeyPath(final CommandLine commandLine) {
        final String privateKeyPath = commandLine.getOptionValue("keys.keyData.privateKeyPath");

        if (privateKeyPath == null) {
            throw new IllegalArgumentException("Private key path cannot be null when updating key password");
        }

        final Path keypath = Paths.get(privateKeyPath);
        if (Files.notExists(keypath)) {
            throw new IllegalArgumentException("Private key path must exist when updating key password");
        }

        return keypath;
    }

    static List<String> passwords(final CommandLine commandLine) throws IOException {
        final String password = commandLine.getOptionValue("keys.passwords");
        final String passwordFile = commandLine.getOptionValue("keys.passwordFile");

        if (password != null) {
            return singletonList(password);
        } else if (passwordFile != null) {
            return Files.readAllLines(Paths.get(passwordFile));
        } else {
            return emptyList();
        }

    }

    static ArgonOptions argonOptions(final CommandLine commandLine) {
        final String algorithm = commandLine.getOptionValue("keys.keyData.config.data.aopts.algorithm", "i");
        final String iterations = commandLine.getOptionValue("keys.keyData.config.data.aopts.iterations", "10");
        final String memory = commandLine.getOptionValue("keys.keyData.config.data.aopts.memory", "1048576");
        final String parallelism = commandLine.getOptionValue("keys.keyData.config.data.aopts.parallelism", "4");

        return new ArgonOptions(
            algorithm, Integer.valueOf(iterations), Integer.valueOf(memory), Integer.valueOf(parallelism)
        );
    }

}
