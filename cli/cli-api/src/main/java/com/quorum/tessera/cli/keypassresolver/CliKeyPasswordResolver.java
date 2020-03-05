package com.quorum.tessera.cli.keypassresolver;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.passwords.PasswordReader;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class CliKeyPasswordResolver implements KeyPasswordResolver {

    private static final int MAX_PASSWORD_ATTEMPTS = 2;

    private final PasswordReader passwordReader;

    public CliKeyPasswordResolver() {
        this(PasswordReaderFactory.create());
    }

    public CliKeyPasswordResolver(final PasswordReader passwordReader) {
        this.passwordReader = Objects.requireNonNull(passwordReader);
    }

    @Override
    public void resolveKeyPasswords(final Config config) {

        final KeyConfiguration input = config.getKeys();
        if (input == null) {
            // invalid config, but gets picked up by validation later
            return;
        }

        final List<String> allPasswords = new ArrayList<>();
        if (input.getPasswords() != null) {
            allPasswords.addAll(input.getPasswords());
        } else if (input.getPasswordFile() != null) {
            try {
                allPasswords.addAll(Files.readAllLines(input.getPasswordFile(), StandardCharsets.UTF_8));
            } catch (final IOException ex) {
                // dont do anything, if any keys are locked validation will complain that
                // locked keys were provided without passwords
                System.err.println("Could not read the password file");
            }
        }

        List<KeyData> keyPairs = input.getKeyData();

        IntStream.range(0, input.getKeyData().size())
                .forEachOrdered(
                        i -> {
                            if (i < allPasswords.size()) {
                                keyPairs.get(i).setPassword(allPasswords.get(i));
                            }
                        });

        // decrypt the keys, either using provided passwords or read from CLI

        EncryptorConfig encryptorConfig =
                Optional.ofNullable(config.getEncryptor())
                        .orElse(
                                new EncryptorConfig() {
                                    {
                                        setType(EncryptorType.NACL);
                                    }
                                });

        final KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

        IntStream.range(0, input.getKeyData().size())
                .forEachOrdered(
                        keyNumber -> getSingleKeyPassword(keyNumber, input.getKeyData().get(keyNumber), keyEncryptor));
    }

    // TODO: make private
    // @VisibleForTesting
    public void getSingleKeyPassword(final int keyNumber, final KeyData keyPair, final KeyEncryptor keyEncryptor) {

        final boolean isInline = KeyDataUtil.isInline(keyPair);
        final boolean isFilesystem = KeyDataUtil.isFileSystem(keyPair);

        if (!isInline && !isFilesystem) {
            // some other key type that doesn't use passwords, skip
            return;
        }

        final boolean isLocked = KeyDataUtil.isLocked(keyPair);

        if (isLocked) {

            ConfigKeyPair configKeyPair = KeyDataUtil.unmarshal(keyPair, keyEncryptor);

            int currentAttemptNumber = MAX_PASSWORD_ATTEMPTS;
            while (currentAttemptNumber > 0) {

                if (Objects.isNull(configKeyPair.getPassword())
                        || configKeyPair.getPassword().isEmpty()
                        || Optional.ofNullable(configKeyPair.getPrivateKey())
                                .filter(s -> s.contains("NACL_FAILURE"))
                                .isPresent()) {

                    final String attemptOutput =
                            "Attempt "
                                    + (MAX_PASSWORD_ATTEMPTS - currentAttemptNumber + 1)
                                    + " of "
                                    + MAX_PASSWORD_ATTEMPTS
                                    + ".";
                    System.out.printf("Password for key[%s] missing or invalid.", keyNumber);
                    System.out.println();
                    System.out.printf("%s Enter a password for the key", attemptOutput);
                    System.out.println();

                    final String pass = passwordReader.readPasswordFromConsole();
                    configKeyPair.withPassword(pass);
                    keyPair.setPassword(pass);
                }
                currentAttemptNumber--;
            }
        }
    }
}
