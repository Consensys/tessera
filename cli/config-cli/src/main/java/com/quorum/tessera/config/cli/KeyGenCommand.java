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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import picocli.CommandLine;

@CommandLine.Command(
    name = "keygen",
    aliases = {"-keygen"},
    header = "Generate Tessera encryption keys",
    descriptionHeading = "%nDescription: ",
    description =
        "Generate one or more new key pairs and store in files or a supported key vault.  The key properties can be configured using the encryptor options.",
    parameterListHeading = "Parameters:%n",
    commandListHeading = "%nCommands:%n",
    optionListHeading = "%nOptions:%n",
    abbreviateSynopsis = true,
    subcommands = {CommandLine.HelpCommand.class})
public class KeyGenCommand implements Callable<CliResult> {

  private final KeyGeneratorFactory keyGeneratorFactory;

  private final ConfigFileUpdaterWriter configFileUpdaterWriter;

  private final PasswordFileUpdaterWriter passwordFileUpdaterWriter;

  private final KeyDataMarshaller keyDataMarshaller;

  private final Validator validator =
      Validation.byDefaultProvider()
          .configure()
          .ignoreXmlConfiguration()
          .buildValidatorFactory()
          .getValidator();

  @CommandLine.Option(
      names = {"--keyout", "-filename"},
      split = ",",
      description =
          "Comma-separated list of paths to save generated key files. Can also be used with keyvault. Number of args determines number of key-pairs generated (default = ${DEFAULT-VALUE})")
  private List<String> keyOut;

  @CommandLine.Option(
      names = {"--argonconfig", "-keygenconfig"},
      description =
          "File containing Argon2 encryption config used to secure the new private key when storing to the filesystem")
  private ArgonOptions argonOptions;

  @CommandLine.ArgGroup(heading = "Key Vault Options:%n", exclusive = false)
  private KeyVaultConfigOptions keyVaultConfigOptions;

  @CommandLine.ArgGroup(exclusive = false)
  private KeyGenFileUpdateOptions fileUpdateOptions;

  @CommandLine.Mixin private EncryptorOptions encryptorOptions;

  @CommandLine.Mixin private DebugOptions debugOptions;

  KeyGenCommand(
      KeyGeneratorFactory keyGeneratorFactory,
      ConfigFileUpdaterWriter configFileUpdaterWriter,
      PasswordFileUpdaterWriter passwordFileUpdaterWriter,
      KeyDataMarshaller keyDataMarshaller) {
    this.keyGeneratorFactory = Objects.requireNonNull(keyGeneratorFactory);
    this.configFileUpdaterWriter = Objects.requireNonNull(configFileUpdaterWriter);
    this.passwordFileUpdaterWriter = Objects.requireNonNull(passwordFileUpdaterWriter);
    this.keyDataMarshaller = Objects.requireNonNull(keyDataMarshaller);
  }

  @Override
  public CliResult call() throws IOException {
    // TODO(cjh) this check shouldn't be required as --configfile is marked as 'required' in
    // KeyGenFileUpdateOptions
    if (Objects.nonNull(fileUpdateOptions) && Objects.isNull(fileUpdateOptions.getConfig())) {
      throw new CliException("Missing required argument(s): --configfile=<config>");
    }

    final EncryptorConfig encryptorConfig =
        Optional.ofNullable(fileUpdateOptions)
            .map(KeyGenFileUpdateOptions::getConfig)
            .map(Config::getEncryptor)
            .orElseGet(
                () ->
                    Optional.ofNullable(encryptorOptions)
                        .map(EncryptorOptions::parseEncryptorConfig)
                        .orElse(EncryptorConfig.getDefault()));

    final KeyVaultOptions keyVaultOptions =
        Optional.ofNullable(keyVaultConfigOptions)
            .map(KeyVaultConfigOptions::getHashicorpSecretEnginePath)
            .map(KeyVaultOptions::new)
            .orElse(null);

    final KeyVaultConfig keyVaultConfig;
    if (keyVaultConfigOptions == null) {
      keyVaultConfig = null;
    } else if (keyVaultConfigOptions.getVaultType() == null) {
      throw new CliException("Key vault type either not provided or not recognised");
    } else if (fileUpdateOptions != null) {
      keyVaultConfig =
          Optional.of(fileUpdateOptions)
              .map(KeyGenFileUpdateOptions::getConfig)
              .map(Config::getKeys)
              .flatMap(c -> c.getKeyVaultConfig(keyVaultConfigOptions.getVaultType()))
              .orElse(null);
    } else {

      final KeyVaultHandler keyVaultHandler = new DispatchingKeyVaultHandler();
      keyVaultConfig = keyVaultHandler.handle(keyVaultConfigOptions);

      if (keyVaultConfig.getKeyVaultType() == KeyVaultType.HASHICORP) {

        if (Objects.isNull(keyOut)) {
          throw new CliException(
              "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
        }
      }

      final Set<ConstraintViolation<KeyVaultConfig>> violations =
          validator.validate(keyVaultConfig);
      if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
      }
    }

    final KeyGenerator keyGenerator = keyGeneratorFactory.create(keyVaultConfig, encryptorConfig);

    final List<String> newKeyNames =
        Optional.ofNullable(keyOut)
            .filter(Predicate.not(List::isEmpty))
            .map(List::copyOf)
            .orElseGet(() -> List.of(""));

    final List<ConfigKeyPair> newConfigKeyPairs =
        newKeyNames.stream()
            .map(name -> keyGenerator.generate(name, argonOptions, keyVaultOptions))
            .collect(Collectors.toList());

    final List<char[]> newPasswords =
        newConfigKeyPairs.stream()
            .filter(Objects::nonNull)
            .map(ConfigKeyPair::getPassword)
            .collect(Collectors.toList());

    final List<KeyData> newKeyData =
        newConfigKeyPairs.stream().map(keyDataMarshaller::marshal).collect(Collectors.toList());

    if (Objects.isNull(fileUpdateOptions)) {
      return new CliResult(0, true, null);
    }

    // prepare config for addition of new keys if required
    prepareConfigForNewKeys(fileUpdateOptions.getConfig());

    if (Objects.nonNull(fileUpdateOptions.getConfigOut())) {
      if (Objects.nonNull(fileUpdateOptions.getPwdOut())) {
        passwordFileUpdaterWriter.updateAndWrite(
            newPasswords, fileUpdateOptions.getConfig(), fileUpdateOptions.getPwdOut());
        fileUpdateOptions.getConfig().getKeys().setPasswordFile(fileUpdateOptions.getPwdOut());
      }
      configFileUpdaterWriter.updateAndWrite(
          newKeyData,
          keyVaultConfig,
          fileUpdateOptions.getConfig(),
          fileUpdateOptions.getConfigOut());
    } else {
      configFileUpdaterWriter.updateAndWriteToCLI(
          newKeyData, keyVaultConfig, fileUpdateOptions.getConfig());
    }

    return new CliResult(0, true, fileUpdateOptions.getConfig());
  }

  static void prepareConfigForNewKeys(Config config) {
    if (Objects.isNull(config.getKeys())) {
      config.setKeys(new KeyConfiguration());
    }
    if (Objects.isNull(config.getKeys().getKeyData())) {
      config.getKeys().setKeyData(new ArrayList<>());
    }
  }
}
