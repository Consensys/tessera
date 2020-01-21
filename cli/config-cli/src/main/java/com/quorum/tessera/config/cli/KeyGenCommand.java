package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.ConfigFileUpdaterWriter;
import com.quorum.tessera.config.util.PasswordFileUpdaterWriter;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import com.quorum.tessera.key.generation.KeyVaultOptions;
import picocli.CommandLine;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "keygen",
        aliases = {"-keygen"},
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Generate Tessera encryption keys",
        abbreviateSynopsis = true,
        subcommands = {CommandLine.HelpCommand.class})
public class KeyGenCommand implements Callable<CliResult> {

    private final KeyGeneratorFactory factory;

    private final ConfigFileUpdaterWriter configFileUpdaterWriter;

    private final PasswordFileUpdaterWriter passwordFileUpdaterWriter;

    private final Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    @CommandLine.Option(
            names = {"--keyout", "-filename"},
            split = ",",
            arity = "0..1",
            description =
                    "Comma-separated list of paths to save generated key files. Can also be used with keyvault. Number of args determines number of key-pairs generated (default = ${DEFAULT-VALUE})")
    public List<String> keyOut;

    @CommandLine.Option(
            names = {"--argonconfig", "-keygenconfig"},
            description =
                    "File containing Argon2 encryption config used to secure the new private key when storing to the filesystem")
    public ArgonOptions argonOptions;

    @CommandLine.ArgGroup(heading = "Key Vault Options:%n", exclusive = false)
    KeyVaultConfigOptions keyVaultConfigOptions;

    @CommandLine.ArgGroup(exclusive = false)
    KeyGenFileUpdateOptions fileUpdateOptions;

    @CommandLine.Mixin public EncryptorOptions encryptorOptions;

    KeyGenCommand(
            KeyGeneratorFactory keyGeneratorFactory,
            ConfigFileUpdaterWriter configFileUpdaterWriter,
            PasswordFileUpdaterWriter passwordFileUpdaterWriter) {
        this.factory = keyGeneratorFactory;
        this.configFileUpdaterWriter = configFileUpdaterWriter;
        this.passwordFileUpdaterWriter = passwordFileUpdaterWriter;
    }

    @Override
    public CliResult call() throws IOException {
        final EncryptorConfig encryptorConfig = this.encryptorConfig().orElse(EncryptorConfig.getDefault());
        final KeyVaultOptions keyVaultOptions = this.keyVaultOptions().orElse(null);
        final KeyVaultConfig keyVaultConfig = this.keyVaultConfig().orElse(null);

        final KeyGenerator generator = factory.create(keyVaultConfig, encryptorConfig);

        final List<String> newKeyNames = new ArrayList<>();

        if (Objects.isNull(keyOut) || keyOut.isEmpty()) {
            newKeyNames.add("");
        } else {
            newKeyNames.addAll(keyOut);
        }

        List<ConfigKeyPair> newKeys =
                newKeyNames.stream()
                        .map(name -> generator.generate(name, argonOptions, keyVaultOptions))
                        .collect(Collectors.toList());

        if (Objects.nonNull(fileUpdateOptions)) {
            // config is a 'required' option in KeyGenFileUpdateOptions
            if (Objects.isNull(fileUpdateOptions.getConfig().getKeys())) {
                fileUpdateOptions.getConfig().setKeys(new KeyConfiguration());
            }
            if (Objects.isNull(fileUpdateOptions.getConfig().getKeys().getKeyData())) {
                fileUpdateOptions.getConfig().getKeys().setKeyData(new ArrayList<>());
            }
            if (Objects.nonNull(fileUpdateOptions.getConfigOut())) {
                if (Objects.nonNull(fileUpdateOptions.getPwdOut())) {
                    passwordFileUpdaterWriter.updateAndWrite(
                            newKeys, fileUpdateOptions.getConfig(), fileUpdateOptions.getPwdOut());
                    fileUpdateOptions.getConfig().getKeys().setPasswordFile(fileUpdateOptions.getPwdOut());
                }
                configFileUpdaterWriter.updateAndWrite(
                        newKeys, keyVaultConfig, fileUpdateOptions.getConfig(), fileUpdateOptions.getConfigOut());
            } else {
                configFileUpdaterWriter.updateAndWriteToCLI(newKeys, keyVaultConfig, fileUpdateOptions.getConfig());
            }
        }

        return new CliResult(0, true, null);
    }

    private Optional<EncryptorConfig> encryptorConfig() {
        Optional<EncryptorConfig> fromConfigFile =
                Optional.ofNullable(fileUpdateOptions)
                        .map(KeyGenFileUpdateOptions::getConfig)
                        .map(Config::getEncryptor);

        Optional<EncryptorConfig> fromCliOptions =
                Optional.ofNullable(encryptorOptions).map(EncryptorOptions::parseEncryptorConfig);

        if (fromConfigFile.isPresent()) {
            return fromConfigFile;
        } else {
            return fromCliOptions;
        }
    }

    private Optional<KeyVaultOptions> keyVaultOptions() {
        if (!Optional.ofNullable(keyVaultConfigOptions)
                .map(KeyVaultConfigOptions::getHashicorpSecretEnginePath)
                .isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new KeyVaultOptions(keyVaultConfigOptions.getHashicorpSecretEnginePath()));
    }

    private Optional<KeyVaultConfig> keyVaultConfig() {
        if (Objects.isNull(keyVaultConfigOptions)) {
            return Optional.empty();
        }

        if (Objects.isNull(keyVaultConfigOptions.getVaultType())) {
            throw new CliException("Key vault type either not provided or not recognised");
        }

        final KeyVaultConfig keyVaultConfig;

        final Optional<DefaultKeyVaultConfig> fromConfigFile =
                Optional.ofNullable(fileUpdateOptions)
                        .map(KeyGenFileUpdateOptions::getConfig)
                        .map(Config::getKeys)
                        .flatMap(c -> c.getKeyVaultConfig(keyVaultConfigOptions.vaultType));

        if (fromConfigFile.isPresent()) {
            return Optional.of(fromConfigFile.get());
        }

        if (KeyVaultType.AZURE.equals(keyVaultConfigOptions.getVaultType())) {
            keyVaultConfig = new AzureKeyVaultConfig(keyVaultConfigOptions.getVaultUrl());

            Set<ConstraintViolation<AzureKeyVaultConfig>> violations =
                    validator.validate((AzureKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else if (KeyVaultType.HASHICORP.equals(keyVaultConfigOptions.getVaultType())) {
            if (Objects.isNull(keyOut) || keyOut.isEmpty()) {
                throw new CliException(
                        "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
            }

            keyVaultConfig =
                    new HashicorpKeyVaultConfig(
                            keyVaultConfigOptions.getVaultUrl(),
                            keyVaultConfigOptions.getHashicorpApprolePath(),
                            keyVaultConfigOptions.getHashicorpTlsKeystore(),
                            keyVaultConfigOptions.getHashicorpTlsTruststore());

            Set<ConstraintViolation<HashicorpKeyVaultConfig>> violations =
                    validator.validate((HashicorpKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else {
            DefaultKeyVaultConfig awsKeyVaultConfig = new DefaultKeyVaultConfig();
            awsKeyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

            Optional.ofNullable(keyVaultConfigOptions.getVaultUrl())
                    .ifPresent(u -> awsKeyVaultConfig.setProperty("endpoint", u));

            keyVaultConfig = awsKeyVaultConfig;

            Set<ConstraintViolation<DefaultKeyVaultConfig>> violations = validator.validate(awsKeyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }

        return Optional.of(keyVaultConfig);
    }
}
