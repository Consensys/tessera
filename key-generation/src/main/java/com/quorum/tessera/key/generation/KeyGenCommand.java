package com.quorum.tessera.key.generation;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.EncryptorOptions;
import com.quorum.tessera.config.*;
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
    subcommands = {CommandLine.HelpCommand.class}
)
public class KeyGenCommand implements Callable<CliResult> {
    private final KeyGeneratorFactory factory = KeyGeneratorFactory.newFactory();

    private final Validator validator =
        Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    // TODO(cjh) raise CLI usage wording changes as separate change

    @CommandLine.Option(
        names = {"--output", "-filename"},
        description = "Comma-separated list of paths to save generated key files. Can also be used with keyvault. Number of args determines number of key-pairs generated."
    )
    public List<String> output;

    // TODO(cjh) review description and name
    @CommandLine.Option(
        names = {"--encryptionconfig", "-keygenconfig"},
        description = "File containing Argon2 encryption config used to secure the new private key"
    )
    public ArgonOptions encryptionConfig;

    @CommandLine.Option(
        names = {"--vault.type", "-keygenvaulttype"},
        description = "Specify the key vault provider the generated key is to be saved in.  If not set, the key will be encrypted and stored on the local filesystem"
    )
    //TODO(cjh) get possible enum values to show in the usage
    public KeyVaultType vaultType;

    @CommandLine.Option(
        names = {"--vault.url", "-keygenvaulturl"},
        description = "Base url for key vault"
    )
    public String vaultUrl;

    @CommandLine.Option(
        names = {"--vault.hashicorp.approlepath", "-keygenvaultapprole"},
        description = "AppRole path for Hashicorp Vault authentication (defaults to 'approle')"
    )
    public String hashicorpApprolePath;

    @CommandLine.Option(
        names = {"--vault.hashicorp.secretenginepath", "-keygenvaultsecretengine"},
        description = "Name of already enabled Hashicorp v2 kv secret engine"
    )
    public String hashicorpSecretEnginePath;

    @CommandLine.Option(
        names = {"--vault.hashicorp.tlskeystore", "-keygenvaultkeystore"},
        description = "Path to JKS keystore for TLS Hashicorp Vault communication"
    )
    public Path hashicorpTlsKeystore;

    @CommandLine.Option(
        names = {"--vault.hashicorp.tlstruststore", "-keygenvaulttruststore"},
        description = "Path to JKS truststore for TLS Hashicorp Vault communication"
    )
    public Path hashicorpTlsTruststore;

    //TODO(cjh) do something about the duplication of the configfile option in each relevant command
    @CommandLine.Option(
        names = {"--configfile", "-configfile"},
        description = "Path to node configuration file"
    )
    public Config config;

    @CommandLine.Mixin
    public EncryptorOptions encryptorOptions;

    @Override
    public CliResult call() throws Exception {
        final EncryptorConfig encryptorConfig;

        if (Optional.ofNullable(config).map(Config::getEncryptor).isPresent()) {
            encryptorConfig = config.getEncryptor();
        } else {
            encryptorConfig = encryptorOptions.parseEncryptorConfig();
        }

        final KeyVaultOptions keyVaultOptions = this.keyVaultOptions().orElse(null);
        final KeyVaultConfig keyVaultConfig = this.keyVaultConfig().orElse(null);

        final KeyGenerator generator = factory.create(keyVaultConfig, encryptorConfig);

        output.forEach(
            name -> generator.generate(name, encryptionConfig, keyVaultOptions)
        );

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

        final KeyVaultConfig keyVaultConfig;

        if (vaultType.equals(KeyVaultType.AZURE)) {
            keyVaultConfig = new AzureKeyVaultConfig(vaultUrl);

            Set<ConstraintViolation<AzureKeyVaultConfig>> violations =
                validator.validate((AzureKeyVaultConfig) keyVaultConfig);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } else {
            if (output.size() == 0) {
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
        }

        return Optional.of(keyVaultConfig);
    }
}
