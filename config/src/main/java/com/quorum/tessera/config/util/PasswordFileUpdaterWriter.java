package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.io.FilesDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;

public class PasswordFileUpdaterWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordFileUpdaterWriter.class);

    private static final Set<PosixFilePermission> NEW_PASSWORD_FILE_PERMS =
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet());

    private static final String passwordsMessage =
            "Configfile must contain \"passwordFile\" field. The \"passwords\" field is no longer supported.";

    private final FilesDelegate filesDelegate;

    public PasswordFileUpdaterWriter(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

    public void updateAndWrite(List<String> newPasswords, Config config, Path pwdDest) throws IOException {
        if (Optional.ofNullable(config).map(Config::getKeys).map(KeyConfiguration::getPasswords).isPresent()
                && !config.getKeys().getPasswords().isEmpty()) {
            throw new ConfigException(new RuntimeException(passwordsMessage));
        }

        if (filesDelegate.exists(pwdDest)) {
            throw new FileAlreadyExistsException(pwdDest.toString());
        }

        LOGGER.info("Writing updated passwords to {}", pwdDest);

        final List<String> passwords;

        if (Optional.ofNullable(config.getKeys()).map(KeyConfiguration::getPasswordFile).isPresent()) {
            passwords = filesDelegate.readAllLines(config.getKeys().getPasswordFile());
        } else {
            LOGGER.info("No existing password file defined in config");
            passwords = new ArrayList<>();

            Optional.ofNullable(config.getKeys())
                    .map(KeyConfiguration::getKeyData)
                    .ifPresent(k -> k.forEach(kk -> passwords.add("")));
        }

        passwords.addAll(newPasswords);

        filesDelegate.createFile(pwdDest);
        LOGGER.info("Created empty file at {}", pwdDest);

        filesDelegate.setPosixFilePermissions(pwdDest, NEW_PASSWORD_FILE_PERMS);
        filesDelegate.write(pwdDest, passwords, APPEND);
        LOGGER.info("Updated passwords written to {}", pwdDest);
    }
}
