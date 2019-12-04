package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.parsers.Parser;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import com.quorum.tessera.key.generation.KeyVaultOptions;
import org.apache.commons.cli.CommandLine;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import java.util.Objects;

public class KeyGenerationParser implements Parser<List<ConfigKeyPair>> {

    private final KeyGeneratorFactory factory = KeyGeneratorFactory.newFactory();

    private final Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final EncryptorConfig encryptorConfig;

    public KeyGenerationParser(EncryptorConfig encryptorConfig) {
        this.encryptorConfig = Objects.requireNonNull(encryptorConfig);
    }

    @Override
    public List<ConfigKeyPair> parse(final CommandLine commandLine) throws IOException {

        final ArgonOptions argonOptions = this.argonOptions(commandLine).orElse(null);
        final KeyVaultOptions keyVaultOptions = this.keyVaultOptions(commandLine).orElse(null);
        final KeyVaultConfig keyVaultConfig = this.keyVaultConfig(commandLine).orElse(null);

        final KeyGenerator generator = factory.create(keyVaultConfig, encryptorConfig);

        if (commandLine.hasOption("keygen")) {
            return this.filenames(commandLine).stream()
                    .map(name -> generator.generate(name, argonOptions, keyVaultOptions))
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

    private Optional<KeyVaultOptions> keyVaultOptions(final CommandLine commandLine) {
        Optional<String> secretEngineName = Optional.ofNullable(commandLine.getOptionValue("keygenvaultsecretengine"));

        return secretEngineName.map(KeyVaultOptions::new);
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
        if (!commandLine.hasOption("keygenvaulttype") && !commandLine.hasOption("keygenvaulturl")) {
            return Optional.empty();
        }

        final String t = commandLine.getOptionValue("keygenvaulttype");

        KeyVaultType keyVaultType;
        try {
            keyVaultType = KeyVaultType.valueOf(t.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CliException("Key vault type either not provided or not recognised");
        }

        String keyVaultUrl = commandLine.getOptionValue("keygenvaulturl");

        KeyVaultConfig keyVaultConfig;

        if (keyVaultType.equals(KeyVaultType.AZURE)) {
            keyVaultConfig = new AzureKeyVaultConfig(keyVaultUrl);

            Set<ConstraintViolation<AzureKeyVaultConfig>> violations =
                    validator.validate((AzureKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else if(keyVaultType.equals(KeyVaultType.HASHICORP)) {
            if (!commandLine.hasOption("filename")) {
                throw new CliException(
                        "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
            }

            String approlePath = commandLine.getOptionValue("keygenvaultapprole");

            Optional<Path> tlsKeyStorePath =
                    Optional.ofNullable(commandLine.getOptionValue("keygenvaultkeystore")).map(Paths::get);

            Optional<Path> tlsTrustStorePath =
                    Optional.ofNullable(commandLine.getOptionValue("keygenvaulttruststore")).map(Paths::get);

            keyVaultConfig =
                    new HashicorpKeyVaultConfig(
                            keyVaultUrl, approlePath, tlsKeyStorePath.orElse(null), tlsTrustStorePath.orElse(null));

            Set<ConstraintViolation<HashicorpKeyVaultConfig>> violations =
                    validator.validate((HashicorpKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else {
            keyVaultConfig = new AWSKeyVaultConfig(keyVaultUrl);
            
            Set<ConstraintViolation<AWSKeyVaultConfig>> violations =
                validator.validate((AWSKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } 

        return Optional.of(keyVaultConfig);
    }
}
