package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.SystemAdapter;
import org.apache.commons.cli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationParser implements Parser<Config> {

    private static final Set<PosixFilePermission> NEW_PASSWORD_FILE_PERMS =
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet());

    private final List<ConfigKeyPair> newlyGeneratedKeys;

    private final FilesDelegate filesDelegate;

    public ConfigurationParser(List<ConfigKeyPair> newlyGeneratedKeys) {
        this(newlyGeneratedKeys, FilesDelegate.create());
    }

    public ConfigurationParser(List<ConfigKeyPair> newlyGeneratedKeys, FilesDelegate filesDelegate) {
        this.newlyGeneratedKeys = Objects.requireNonNull(newlyGeneratedKeys);
        this.filesDelegate = filesDelegate;
    }

    @Override
    public Config parse(final CommandLine commandLine) throws IOException {

        Config config = null;

        final boolean isGeneratingWithKeyVault =
                commandLine.hasOption("keygen") && commandLine.hasOption("keygenvaulturl");

        if (commandLine.hasOption("configfile") && !isGeneratingWithKeyVault) {
            final Path path = Paths.get(commandLine.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = filesDelegate.newInputStream(path)) {
                config = JaxbUtil.unmarshal(in, Config.class);

                if (!newlyGeneratedKeys.isEmpty()) {
                    if (config.getKeys() == null) {
                        KeyConfiguration keyConfiguration = new KeyConfiguration();
                        keyConfiguration.setKeyData(new ArrayList<>());
                        keyConfiguration.setPasswords(new ArrayList<>());
                        config.setKeys(keyConfiguration);
                    }
                    doPasswordStuff(config, newlyGeneratedKeys);
                    config.getKeys().getKeyData().addAll(newlyGeneratedKeys);
                }
            }

            if (!newlyGeneratedKeys.isEmpty()) {
                // we have generated new keys, so we need to output the new configuration
                output(commandLine, config);
            }

            ConfigFileStore.create(path);
        }

        return config;
    }

    private static void output(CommandLine commandLine, Config config) throws IOException {

        if (commandLine.hasOption("output")) {
            final Path outputConfigFile = Paths.get(commandLine.getOptionValue("output"));

            try (OutputStream out = Files.newOutputStream(outputConfigFile, CREATE_NEW)) {
                JaxbUtil.marshal(config, out);
            }
        } else {
            JaxbUtil.marshal(config, SystemAdapter.INSTANCE.out());
        }
    }

    // create a file if it doesn't exist and set the permissions to be only
    // read/write for the creator
    private Path createFile(final Path fileToMake) {
        if (filesDelegate.notExists(fileToMake)) {
            filesDelegate.createFile(fileToMake);
            return filesDelegate.setPosixFilePermissions(fileToMake, NEW_PASSWORD_FILE_PERMS);
        }
        return fileToMake;
    }

    public Config doPasswordStuff(Config config, List<ConfigKeyPair> newKeys) {

        try {
            final List<String> newPasswords =
                    newKeys.stream().map(ConfigKeyPair::getPassword).collect(Collectors.toList());

            if (config.getKeys().getPasswords() != null) {
                config.getKeys().getPasswords().addAll(newPasswords);
            } else if (config.getKeys().getPasswordFile() != null) {
                createFile(config.getKeys().getPasswordFile());
                filesDelegate.write(config.getKeys().getPasswordFile(), newPasswords, APPEND);
            } else if (!newPasswords.stream().allMatch(Objects::isNull)) {
                final List<String> existingPasswords =
                        config.getKeys().getKeyData().stream().map(k -> "").collect(Collectors.toList());
                existingPasswords.addAll(newPasswords);

                Path passwordsFile = createFile(Paths.get("passwords.txt"));
                filesDelegate.write(passwordsFile, existingPasswords, APPEND);

                return new Config(
                        config.getJdbcConfig(),
                        config.getServerConfigs(),
                        config.getPeers(),
                        new KeyConfiguration(
                                passwordsFile,
                                null,
                                config.getKeys().getKeyData(),
                                config.getKeys().getAzureKeyVaultConfig(),
                                config.getKeys().getHashicorpKeyVaultConfig()),
                        config.getAlwaysSendTo(),
                        config.getUnixSocketFile(),
                        config.isUseWhiteList(),
                        config.isDisablePeerDiscovery());
            }
        } catch (final UncheckedIOException ex) {
            // TODO : Check is this is used to feedbeck message to cli and remove
            throw new RuntimeException("Could not store new passwords: " + ex.getMessage());
        }

        return config;
    }
}
