package com.quorum.tessera.cli.keypassresolver;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.util.PasswordReader;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.io.SystemAdapter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public void resolveKeyPasswords(final Config config) {

        final KeyConfiguration input = config.getKeys();
        if (input == null) {
            //invalid config, but gets picked up by validation later
            return;
        }

        final List<String> allPasswords = new ArrayList<>();
        if (input.getPasswords() != null) {
            allPasswords.addAll(input.getPasswords());
        } else if (input.getPasswordFile() != null) {
            try {
                allPasswords.addAll(Files.readAllLines(input.getPasswordFile(), StandardCharsets.UTF_8));
            } catch (final IOException ex) {
                //dont do anything, if any keys are locked validation will complain that
                //locked keys were provided without passwords
                SystemAdapter.INSTANCE.err().println("Could not read the password file");
            }
        }

        IntStream
            .range(0, input.getKeyData().size())
            .forEachOrdered(i -> {
                if(i < allPasswords.size()) {
                    input.getKeyData().get(i).withPassword(allPasswords.get(i));
                }
            });

        //decrypt the keys, either using provided passwords or read from CLI
        IntStream
            .range(0, input.getKeyData().size())
            .forEachOrdered(keyNumber -> getSingleKeyPassword(keyNumber, input.getKeyData().get(keyNumber)));
    }

    //TODO: make private
    //@VisibleForTesting
    public void getSingleKeyPassword(final int keyNumber, final ConfigKeyPair keyPair) {
        final boolean isInline = keyPair instanceof InlineKeypair;
        final boolean isFilesystem = keyPair instanceof FilesystemKeyPair;

        if (!isInline && !isFilesystem) {
            //some other key type that doesn't use passwords, skip
            return;
        }

        final InlineKeypair inlineKey = isInline ? (InlineKeypair)keyPair : ((FilesystemKeyPair)keyPair).getInlineKeypair();

        if(inlineKey == null) {
            //filesystem key pair that couldn't load the keys, catch in validation later
            return;
        }

        final boolean isLocked = inlineKey.getPrivateKeyConfig().getType() == PrivateKeyType.LOCKED;

        if (isLocked) {
            int currentAttemptNumber = MAX_PASSWORD_ATTEMPTS;
            while (currentAttemptNumber > 0) {
                if (StringUtils.isEmpty(keyPair.getPassword()) || keyPair.getPrivateKey() == null || keyPair.getPrivateKey().contains("NACL_FAILURE")) {
                    final String attemptOutput = "Attempt " + (MAX_PASSWORD_ATTEMPTS-currentAttemptNumber+1) + " of " + MAX_PASSWORD_ATTEMPTS + ".";
                    System.out.println("Password for key[" + keyNumber + "] missing or invalid.");
                    System.out.println(attemptOutput + " Enter a password for the key");
                    final String pass = passwordReader.readPasswordFromConsole();
                    keyPair.withPassword(pass);
                }
                currentAttemptNumber--;
            }
        }
    }
}
