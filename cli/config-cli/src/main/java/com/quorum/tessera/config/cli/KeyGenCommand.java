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
import java.nio.file.Path;
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

    @CommandLine.Option(
            names = {"--vault.type", "-keygenvaulttype"},
            description =
                    "Specify the key vault provider the generated key is to be saved in.  If not set, the key will be encrypted and stored on the local filesystem.  Valid values: ${COMPLETION-CANDIDATES})")
    public KeyVaultType vaultType;

    @CommandLine.Option(
            names = {"--vault.url", "-keygenvaulturl"},
            description = "Base url for key vault")
    public String vaultUrl;

    @CommandLine.Option(
            names = {"--vault.hashicorp.approlepath", "-keygenvaultapprole"},
            description = "AppRole path for Hashicorp Vault authentication (defaults to 'approle')")
    public String hashicorpApprolePath;

    @CommandLine.Option(
            names = {"--vault.hashicorp.secretenginepath", "-keygenvaultsecretengine"},
            description = "Name of already enabled Hashicorp v2 kv secret engine")
    public String hashicorpSecretEnginePath;

    @CommandLine.Option(
            names = {"--vault.hashicorp.tlskeystore", "-keygenvaultkeystore"},
            description = "Path to JKS keystore for TLS Hashicorp Vault communication")
    public Path hashicorpTlsKeystore;

    @CommandLine.Option(
            names = {"--vault.hashicorp.tlstruststore", "-keygenvaulttruststore"},
            description = "Path to JKS truststore for TLS Hashicorp Vault communication")
    public Path hashicorpTlsTruststore;

    @CommandLine.ArgGroup(exclusive = false)
    KeyGenFileUpdateOptions fileUpdateOptions;

    @CommandLine.Mixin public EncryptorOptions encryptorOptions;

    KeyGenCommand(KeyGeneratorFactory keyGeneratorFactory, ConfigFileUpdaterWriter configFileUpdaterWriter, PasswordFileUpdaterWriter passwordFileUpdaterWriter) {
        this.factory = keyGeneratorFactory;
        this.configFileUpdaterWriter = configFileUpdaterWriter;
        this.passwordFileUpdaterWriter = passwordFileUpdaterWriter;
    }

    @Override
    public CliResult call() throws IOException {
        final EncryptorConfig encryptorConfig;

        if (Optional.ofNullable(fileUpdateOptions)
                .map(KeyGenFileUpdateOptions::getConfig)
                .map(Config::getEncryptor)
                .isPresent()) {
            encryptorConfig = fileUpdateOptions.config.getEncryptor();
        } else {
            encryptorConfig = encryptorOptions.parseEncryptorConfig();
        }

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
            if (Objects.isNull(fileUpdateOptions.getConfig().getKeys())) {
                fileUpdateOptions.getConfig().setKeys(new KeyConfiguration());
            }
            if (Objects.isNull(fileUpdateOptions.getConfig().getKeys().getKeyData())) {
                fileUpdateOptions.getConfig().getKeys().setKeyData(new ArrayList<>());
            }
            if (Objects.nonNull(fileUpdateOptions.getPwdOut())) {
                passwordFileUpdaterWriter.updateAndWrite(newKeys, fileUpdateOptions.getConfig(), fileUpdateOptions.getPwdOut());
                fileUpdateOptions.getConfig().getKeys().setPasswordFile(fileUpdateOptions.getPwdOut());
            }
            configFileUpdaterWriter.updateAndWrite(newKeys, fileUpdateOptions.getConfig(), fileUpdateOptions.getConfigOut());
        }

        return new CliResult(0, true, null);
    }

    private Optional<KeyVaultOptions> keyVaultOptions() {
        if (Objects.isNull(hashicorpSecretEnginePath)) {
            return Optional.empty();
        }

        return Optional.of(new KeyVaultOptions(hashicorpSecretEnginePath));
    }

    private Optional<KeyVaultConfig> keyVaultConfig() {
        if (Objects.isNull(vaultType) && Objects.isNull(vaultUrl)) {
            return Optional.empty();
        }

        if (Objects.isNull(vaultType)) {
            throw new CliException("Key vault type either not provided or not recognised");
        }

        final KeyVaultConfig keyVaultConfig;

        if (vaultType.equals(KeyVaultType.AZURE)) {
            keyVaultConfig = new AzureKeyVaultConfig(vaultUrl);

            Set<ConstraintViolation<AzureKeyVaultConfig>> violations =
                    validator.validate((AzureKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else if (vaultType.equals(KeyVaultType.HASHICORP)) {
            if (Objects.isNull(keyOut) || keyOut.isEmpty()) {
                throw new CliException(
                        "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
            }

            keyVaultConfig =
                    new HashicorpKeyVaultConfig(
                            vaultUrl, hashicorpApprolePath, hashicorpTlsKeystore, hashicorpTlsTruststore);

            Set<ConstraintViolation<HashicorpKeyVaultConfig>> violations =
                    validator.validate((HashicorpKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else {
            DefaultKeyVaultConfig awsKeyVaultConfig = new DefaultKeyVaultConfig();
            awsKeyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

            if (Objects.nonNull(vaultUrl)) {
                awsKeyVaultConfig.setProperty("endpoint", vaultUrl);
            }

            keyVaultConfig = awsKeyVaultConfig;

            Set<ConstraintViolation<DefaultKeyVaultConfig>> violations = validator.validate(awsKeyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }

        return Optional.of(keyVaultConfig);
    }
}
