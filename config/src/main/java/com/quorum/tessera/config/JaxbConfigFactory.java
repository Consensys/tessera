package com.quorum.tessera.config;

import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;

public class JaxbConfigFactory implements ConfigFactory {

    private static final Set<PosixFilePermission> NEW_PASSWORD_FILE_PERMS = Stream
        .of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
        .collect(Collectors.toSet());
    
    @Override
    public Config create(final InputStream configData, final List<ConfigKeyPair> newKeys) {

        final Config config = JaxbUtil.unmarshal(configData, Config.class);

        boolean createdNewPasswordFile = false;

        if (Objects.nonNull(config.getKeys()) && !newKeys.isEmpty()) {
            try {
                final List<String> newPasswords = newKeys
                    .stream()
                    .map(ConfigKeyPair::getPassword)
                    .collect(Collectors.toList());

                if (config.getKeys().getPasswords() != null) {
                    config.getKeys().getPasswords().addAll(newPasswords);
                } else if (config.getKeys().getPasswordFile() != null) {
                    this.createFile(config.getKeys().getPasswordFile());
                    Files.write(config.getKeys().getPasswordFile(), newPasswords, APPEND);
                } else if (!newPasswords.stream().allMatch(""::equals)) {
                    final List<String> existingPasswords = config
                        .getKeys()
                        .getKeyData()
                        .stream()
                        .map(k -> "")
                        .collect(Collectors.toList());
                    existingPasswords.addAll(newPasswords);

                    this.createFile(Paths.get("passwords.txt"));
                    Files.write(Paths.get("passwords.txt"), existingPasswords, APPEND);
                    createdNewPasswordFile = true;
                }
            } catch (final IOException ex) {
                throw new RuntimeException("Could not store new passwords: " + ex.getMessage());
            }

            config.getKeys().getKeyData().addAll(newKeys);

        }

        if (createdNewPasswordFile) {
            //return a new object with the password file set
            return new Config(
                    config.getJdbcConfig(),
                    config.getServerConfig(),
                    config.getPeers(),
                    new KeyConfiguration(Paths.get("passwords.txt"), null, config.getKeys().getKeyData()),
                    config.getAlwaysSendTo(),
                    config.getUnixSocketFile(),
                    config.isUseWhiteList(),
                    config.isDisablePeerDiscovery()
            );
        } else {
            //leave config untouched since it wasn't needed to make a new one
            return config;
        }
    }

    //create a file if it doesn't exist and set the permissions to be only
    // read/write for the creator
    private void createFile(final Path fileToMake) throws IOException {
        if (Files.notExists(fileToMake)) {
            Files.createFile(fileToMake);
            Files.setPosixFilePermissions(fileToMake, NEW_PASSWORD_FILE_PERMS);
        }
    }

}
