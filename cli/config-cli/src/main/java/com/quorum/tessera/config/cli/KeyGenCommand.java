package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import com.quorum.tessera.key.generation.KeyVaultOptions;
import picocli.CommandLine;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

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
    private KeyGeneratorFactory factory;

    private final Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    @CommandLine.Option(
            names = {"--keyout", "-filename"},
            split = ",",
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

    @CommandLine.Option(
            names = {"--configfile", "-configfile"},
            description = "Path to node configuration file")
    public Config config;

    // TODO(cjh) implement config output and password file update ?
    //  we've removed the ability to start the node straight away after generating keys.  Not sure if updating
    //  configfile
    //  and password file is something we want to still support or put onus on users to go and update as required
    @CommandLine.Option(
            names = {"--configout", "-output"},
            description =
                    "Path to save updated configfile to.  Updated config will be printed to terminal if not provided.  Only valid if --configfile option also provided.")
    public List<String> configOut;

    @CommandLine.Mixin public EncryptorOptions encryptorOptions;

    KeyGenCommand(KeyGeneratorFactory keyGeneratorFactory) {
        this.factory = keyGeneratorFactory;
    }

    // TODO(cjh) 'tessera keygen' with no args prints help.  this is consistent with the other commands' behaviour, but
    //  previously this would generate keys at the default location '.'.  do we want to reintroduce this or keep
    //  consistency with the other commands?
    @Override
    public CliResult call() {
        final EncryptorConfig encryptorConfig;

        if (Optional.ofNullable(config).map(Config::getEncryptor).isPresent()) {
            encryptorConfig = config.getEncryptor();
        } else {
            encryptorConfig = encryptorOptions.parseEncryptorConfig();
        }

        final KeyVaultOptions keyVaultOptions = this.keyVaultOptions().orElse(null);
        final KeyVaultConfig keyVaultConfig = this.keyVaultConfig().orElse(null);

        final KeyGenerator generator = factory.create(keyVaultConfig, encryptorConfig);

        if (Objects.isNull(keyOut) || keyOut.isEmpty()) {
            generator.generate("", argonOptions, keyVaultOptions);
        } else {
            keyOut.forEach(name -> generator.generate(name, argonOptions, keyVaultOptions));
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
