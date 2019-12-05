package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ConfigurationParser implements Parser<Config> {

    protected static final Set<PosixFilePermission> NEW_PASSWORD_FILE_PERMS =
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet());

    protected static final String passwordsMessage = "Configfile must contain \"passwordFile\" field. The \"passwords\" field is no longer supported.";

    private final List<ConfigKeyPair> newlyGeneratedKeys;

    private final FilesDelegate filesDelegate;

    public ConfigurationParser(List<ConfigKeyPair> newlyGeneratedKeys) {
        this(newlyGeneratedKeys, FilesDelegate.create());
    }

    protected ConfigurationParser(List<ConfigKeyPair> newlyGeneratedKeys, FilesDelegate filesDelegate) {
        this.newlyGeneratedKeys = Objects.requireNonNull(newlyGeneratedKeys);
        this.filesDelegate = Objects.requireNonNull(filesDelegate);
    }

    @Override
    public Config parse(final CommandLine commandLine) throws IOException {

        Config config = null;

        final boolean isGeneratingWithKeyVault =
                commandLine.hasOption("keygen") && commandLine.hasOption("keygenvaulturl");

        if (commandLine.hasOption("configfile") && !isGeneratingWithKeyVault) {
            final Path path = Paths.get(commandLine.getOptionValue("configfile"));

            if (!filesDelegate.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = filesDelegate.newInputStream(path)) {
                config = JaxbUtil.unmarshal(in, Config.class);

                if (!newlyGeneratedKeys.isEmpty()) {
                    if (config.getKeys() == null) {
                        config.setKeys(new KeyConfiguration());
                        config.getKeys().setKeyData(new ArrayList<>());
                    }
                    if (config.getKeys().getKeyData() == null) {
                        config.getKeys().setKeyData(new ArrayList<>());
                    }
                    doPasswordStuff(config);
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

    private void output(CommandLine commandLine, Config config) throws IOException {

        if (commandLine.hasOption("output")) {
            final Path outputConfigFile = Paths.get(commandLine.getOptionValue("output"));

            try (OutputStream out = filesDelegate.newOutputStream(outputConfigFile, CREATE_NEW)) {
                JaxbUtil.marshal(config, out);
            }
        } else {
            JaxbUtil.marshal(config, SystemAdapter.INSTANCE.out());
        }
    }

    // create a file if it doesn't exist and set the permissions to be only
    // read/write for the creator
    private void createFile(Path fileToMake) {
        boolean notExists = filesDelegate.notExists(fileToMake);
        if (notExists) {
            filesDelegate.createFile(fileToMake);
            filesDelegate.setPosixFilePermissions(fileToMake, NEW_PASSWORD_FILE_PERMS);
        }
    }

    public Config doPasswordStuff(Config config) throws ConfigException {
        final List<String> newPasswords =
            newlyGeneratedKeys.stream().map(ConfigKeyPair::getPassword).collect(Collectors.toList());

        boolean hasNewPasswords = newPasswords.stream().anyMatch(p -> Objects.nonNull(p) && !p.isEmpty());
        boolean isUsingPasswordFile = Objects.nonNull(config.getKeys().getPasswordFile());

        if (hasNewPasswords) {
            if (!isUsingPasswordFile) {
                throw new ConfigException(new RuntimeException(passwordsMessage));
            }

            Path passwordFile = config.getKeys().getPasswordFile();
            createFile(passwordFile);
            filesDelegate.write(passwordFile, newPasswords, APPEND);
        }

        return config;
    }
}
