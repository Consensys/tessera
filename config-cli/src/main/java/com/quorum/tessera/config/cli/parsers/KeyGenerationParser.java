package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class KeyGenerationParser implements Parser<List<ConfigKeyPair>> {

    private final KeyGeneratorFactory factory = KeyGeneratorFactory.newFactory();

    public List<ConfigKeyPair> parse(final CommandLine commandLine) throws IOException {

        final ArgonOptions argonOptions = this.argonOptions(commandLine).orElse(null);
        final KeyVaultConfig keyVaultConfig = this.keyVaultConfig(commandLine).orElse(null);
        final EnvironmentVariableProvider envProvider = new EnvironmentVariableProvider();

        final KeyGenerator generator = factory.create(keyVaultConfig, envProvider);

        if (commandLine.hasOption("keygen")) {
            return this.filenames(commandLine)
                .stream()
                .map(name -> generator.generate(name, argonOptions))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();

    }

    private Optional<ArgonOptions> argonOptions(final CommandLine commandLine) throws IOException {

        if (commandLine.hasOption("keygenconfig")) {
            final String pathName = commandLine.getOptionValue("keygenconfig");
            final InputStream configStream = Files.newInputStream(Paths.get(pathName));

            final ArgonOptions argonOptions = JaxbUtil.unmarshal(configStream, ArgonOptions.class);
            return Optional.of(argonOptions);
        }

        return Optional.empty();
    }

    private List<String> filenames(final CommandLine commandLine) {

        if (commandLine.hasOption("filename")) {

            final String keyNames = commandLine.getOptionValue("filename");
            if (keyNames != null) {
                return Stream.of(keyNames.split(",")).collect(Collectors.toList());
            }

        }

        return singletonList("");

    }

    private Optional<KeyVaultConfig> keyVaultConfig(CommandLine commandLine) {
        if(commandLine.hasOption("keygenvaulturl")) {
            final String vaultUrl = commandLine.getOptionValue("keygenvaulturl");

            return Optional.of(new KeyVaultConfig(vaultUrl));
        }
        return Optional.empty();
    }

}
