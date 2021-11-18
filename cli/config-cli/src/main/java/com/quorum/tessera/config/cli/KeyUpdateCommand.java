package com.quorum.tessera.config.cli;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.passwords.PasswordReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "keyupdate",
    aliases = {"-updatepassword"},
    header = "Update the password for a key",
    descriptionHeading = "%nDescription: ",
    description =
        "Change the password or update the encryption options for an already locked key, or apply a new password to an unlocked key",
    commandListHeading = "%nCommands:%n",
    optionListHeading = "%nOptions:%n",
    abbreviateSynopsis = true,
    subcommands = {CommandLine.HelpCommand.class})
public class KeyUpdateCommand implements Callable<CliResult> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyUpdateCommand.class);

  @CommandLine.Option(names = "--keys.keyData.privateKeyPath", required = true)
  public Path privateKeyPath;

  static String invalidArgonAlgorithmMsg =
      "Allowed values for --keys.keyData.config.data.aopts.algorithm are 'i', 'd' or 'id'";

  @CommandLine.Option(names = "--keys.keyData.config.data.aopts.algorithm", defaultValue = "i")
  public String algorithm;

  @CommandLine.Option(names = "--keys.keyData.config.data.aopts.iterations", defaultValue = "10")
  public Integer iterations;

  @CommandLine.Option(names = "--keys.keyData.config.data.aopts.memory", defaultValue = "1048576")
  public Integer memory;

  @CommandLine.Option(names = "--keys.keyData.config.data.aopts.parallelism", defaultValue = "4")
  public Integer parallelism;

  // TODO(cjh) remove plaintext passwords being provided on CLI, replace with prompt and password
  // file
  @CommandLine.Option(names = {"--keys.passwords"})
  public String password;

  @CommandLine.Option(names = {"--keys.passwordFile"})
  public Path passwordFile;

  @CommandLine.Option(
      names = {"--configfile", "-configfile", "--config-file"},
      description = "Path to node configuration file")
  public Config config;

  @CommandLine.Mixin public EncryptorOptions encryptorOptions;

  @CommandLine.Mixin public DebugOptions debugOptions;

  private KeyEncryptorFactory keyEncryptorFactory;

  // TODO(cjh) is package-private for migrated apache commons CLI tests
  KeyEncryptor keyEncryptor;

  private PasswordReader passwordReader;

  KeyUpdateCommand(KeyEncryptorFactory keyEncryptorFactory, PasswordReader passwordReader) {
    this.keyEncryptorFactory = keyEncryptorFactory;
    this.passwordReader = passwordReader;
  }

  @Override
  public CliResult call() throws Exception {
    final EncryptorConfig encryptorConfig;

    if (Optional.ofNullable(config).map(Config::getEncryptor).isPresent()) {
      encryptorConfig = config.getEncryptor();
    } else {
      encryptorConfig = encryptorOptions.parseEncryptorConfig();
    }

    this.keyEncryptor = keyEncryptorFactory.create(encryptorConfig);

    return execute();
  }

  public CliResult execute() throws IOException {
    final ArgonOptions argonOptions = argonOptions();
    final List<char[]> passwords = passwords();
    final Path keypath = privateKeyPath();

    final KeyDataConfig keyDataConfig =
        JaxbUtil.unmarshal(Files.newInputStream(keypath), KeyDataConfig.class);
    final PrivateKey privateKey = this.getExistingKey(keyDataConfig, passwords);

    final char[] newPassword = passwordReader.requestUserPassword();

    final KeyDataConfig updatedKey;
    if (newPassword.length == 0) {
      final PrivateKeyData privateKeyData =
          new PrivateKeyData(privateKey.encodeToBase64(), null, null, null, null);
      updatedKey = new KeyDataConfig(privateKeyData, PrivateKeyType.UNLOCKED);
    } else {
      final PrivateKeyData privateKeyData =
          keyEncryptor.encryptPrivateKey(privateKey, newPassword, argonOptions);
      updatedKey = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);
    }

    // write the key to file
    Files.write(keypath, JaxbUtil.marshalToString(updatedKey).getBytes(UTF_8));
    System.out.println("Private key at " + keypath.toString() + " updated.");

    return new CliResult(0, true, null);
  }

  PrivateKey getExistingKey(final KeyDataConfig kdc, final List<char[]> passwords) {

    if (kdc.getType() == PrivateKeyType.UNLOCKED) {
      byte[] privateKeyData = Base64.getDecoder().decode(kdc.getValue().getBytes(UTF_8));
      return PrivateKey.from(privateKeyData);
    } else {

      for (final char[] pass : passwords) {
        try {
          return PrivateKey.from(
              keyEncryptor.decryptPrivateKey(kdc.getPrivateKeyData(), pass).getKeyBytes());
        } catch (final Exception e) {
          LOGGER.debug("Password failed to decrypt. Trying next if available.");
        }
      }

      throw new IllegalArgumentException("Locked key but no valid password given");
    }
  }

  Path privateKeyPath() {
    if (Files.notExists(privateKeyPath)) {
      throw new IllegalArgumentException("Private key path must exist when updating key password");
    }

    return privateKeyPath;
  }

  List<char[]> passwords() throws IOException {
    if (password != null) {
      return singletonList(password.toCharArray());
    } else if (passwordFile != null) {
      return Files.readAllLines(passwordFile).stream()
          .map(String::toCharArray)
          .collect(Collectors.toList());
    } else {
      return emptyList();
    }
  }

  ArgonOptions argonOptions() {
    if ("i".equals(algorithm) || "d".equals(algorithm) || "id".equals(algorithm)) {
      return new ArgonOptions(
          algorithm,
          Integer.valueOf(iterations),
          Integer.valueOf(memory),
          Integer.valueOf(parallelism));
    }

    throw new CliException(invalidArgonAlgorithmMsg);
  }
}
