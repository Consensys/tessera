package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import org.apache.commons.cli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ConfigurationParser implements Parser<Config> {

    private List<ConfigKeyPair> newlyGeneratedKeys = Collections.emptyList();

    public ConfigurationParser withNewKeys(final List<ConfigKeyPair> newKeys) {
        this.newlyGeneratedKeys = Objects.requireNonNull(newKeys);
        return this;
    }

    @Override
    public Config parse(final CommandLine commandLine) throws IOException {

        final ConfigFactory configFactory = ConfigFactory.create();

        Config config = null;

        if (commandLine.hasOption("configfile")) {
            final Path path = Paths.get(commandLine.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = Files.newInputStream(path)) {
                config = configFactory.create(in, newlyGeneratedKeys);
            }

            if (!newlyGeneratedKeys.isEmpty()) {
                //we have generated new keys, so we need to output the new configuration
                output(commandLine, config);
            }

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
            JaxbUtil.marshal(config, System.out);
        }

    }
}
