package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.ConfigFileUpdaterWriter;
import com.quorum.tessera.config.util.PasswordFileUpdaterWriter;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

public class KeyGenCommandTest {

  private KeyGeneratorFactory keyGeneratorFactory;

  private ConfigFileUpdaterWriter configFileUpdaterWriter;

  private PasswordFileUpdaterWriter passwordFileUpdaterWriter;

  private KeyDataMarshaller keyDataMarshaller;

  private KeyGenCommand keyGenCommand;

  private KeyGenerator keyGenerator;

  private CliExecutionExceptionHandler executionExceptionHandler;

  private CommandLine commandLine;

  @Before
  public void beforeTest() {
    keyGeneratorFactory = mock(KeyGeneratorFactory.class);
    configFileUpdaterWriter = mock(ConfigFileUpdaterWriter.class);
    passwordFileUpdaterWriter = mock(PasswordFileUpdaterWriter.class);
    keyDataMarshaller = mock(KeyDataMarshaller.class);

    keyGenCommand =
        new KeyGenCommand(
            keyGeneratorFactory,
            configFileUpdaterWriter,
            passwordFileUpdaterWriter,
            keyDataMarshaller);

    keyGenerator = mock(KeyGenerator.class);

    executionExceptionHandler = new CliExecutionExceptionHandler();

    commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(keyGeneratorFactory);
    verifyNoMoreInteractions(configFileUpdaterWriter);
    verifyNoMoreInteractions(passwordFileUpdaterWriter);
    verifyNoMoreInteractions(keyDataMarshaller);
    verifyNoMoreInteractions(keyGenerator);
  }

  @Test
  public void noArgsProvided() throws Exception {

    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(keyGenerator.generate("", null, null)).thenReturn(configKeyPair);

    when(keyGeneratorFactory.create(refEq(null), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    int exitCode = commandLine.execute();
    assertThat(exitCode).isZero();

    CommandLine.ParseResult parseResult = commandLine.getParseResult();
    assertThat(parseResult).isNotNull();
    assertThat(parseResult.matchedArgs()).isEmpty();
    assertThat(parseResult.unmatched()).isEmpty();

    CliResult result = commandLine.getExecutionResult();
    assertThat(result).isNotNull();

    assertThat(result.isSuppressStartup()).isTrue();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);

    verify(keyDataMarshaller).marshal(configKeyPair);
    verify(keyGeneratorFactory).create(refEq(null), any(EncryptorConfig.class));

    verify(keyGenerator).generate("", null, null);
  }

  @Test
  public void updateNoOutputFileDefined() {

    String filename = "";

    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(keyGenerator.generate(filename, null, null)).thenReturn(configKeyPair);

    when(keyGeneratorFactory.create(refEq(null), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

    KeyData keyData = mock(KeyData.class);
    when(keyDataMarshaller.marshal(configKeyPair)).thenReturn(keyData);

    when(config.getKeys()).thenReturn(keyConfiguration);

    commandLine.registerConverter(Config.class, value -> config);

    int exitCode =
        commandLine.execute(
            "--configfile=".concat(filename), "--vault.type=".concat(KeyVaultType.AZURE.name()));
    assertThat(exitCode).isZero();

    verify(keyGeneratorFactory).create(refEq(null), any(EncryptorConfig.class));
    verify(keyGenerator).generate(filename, null, null);

    verify(configFileUpdaterWriter).updateAndWriteToCLI(List.of(keyData), null, config);

    verify(keyDataMarshaller).marshal(configKeyPair);
  }

  @Test
  public void updateFileStuffWithOutputFile() throws Exception {

    String filename = "";

    char[] password = "I LOVE SPARROWS".toCharArray();
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(configKeyPair.getPassword()).thenReturn(password);
    when(keyGenerator.generate(filename, null, null)).thenReturn(configKeyPair);

    when(keyGeneratorFactory.create(refEq(null), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    KeyData keyData = mock(KeyData.class);
    when(keyDataMarshaller.marshal(configKeyPair)).thenReturn(keyData);
    when(config.getKeys()).thenReturn(keyConfiguration);

    commandLine.registerConverter(Config.class, value -> config);

    int exitCode =
        commandLine.execute(
            "--configfile=".concat(filename),
            "--vault.type=".concat(KeyVaultType.AZURE.name()),
            "--configout=".concat("config.out"),
            "--pwdout=".concat("pwd.out"));
    assertThat(exitCode).isZero();

    verify(keyGeneratorFactory).create(refEq(null), any(EncryptorConfig.class));
    verify(keyGenerator).generate(filename, null, null);

    verify(configFileUpdaterWriter)
        .updateAndWrite(List.of(keyData), null, config, Paths.get("config.out"));

    verify(keyDataMarshaller).marshal(configKeyPair);

    verify(passwordFileUpdaterWriter)
        .updateAndWrite(List.of(password), config, Paths.get("pwd.out"));
  }

  @Test
  public void onlySingleOutputFileProvided() throws Exception {

    List<String> optionVariations = List.of("--keyout", "-filename");

    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(keyGenerator.generate("myfile", null, null)).thenReturn(configKeyPair);
    when(keyGeneratorFactory.create(refEq(null), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    for (String option : optionVariations) {
      String arg = option.concat("=myfile");

      int exitCode = commandLine.execute(arg);
      assertThat(exitCode).isZero();

      CommandLine.ParseResult parseResult = commandLine.getParseResult();

      assertThat(parseResult).isNotNull();
      assertThat(parseResult.matchedArgs()).hasSize(1);
      assertThat(parseResult.hasMatchedOption("--keyout"));
      assertThat(parseResult.unmatched()).isEmpty();

      CliResult result = commandLine.getExecutionResult();
      assertThat(result).isNotNull();
      assertThat(result.isSuppressStartup()).isTrue();
      assertThat(result.getConfig()).isNotPresent();
      assertThat(result.getStatus()).isEqualTo(0);
    }

    verify(keyDataMarshaller, times(optionVariations.size())).marshal(configKeyPair);
    verify(keyGeneratorFactory, times(optionVariations.size()))
        .create(refEq(null), any(EncryptorConfig.class));

    verify(keyGenerator, times(optionVariations.size())).generate("myfile", null, null);
  }

  @Test
  public void onlyMulipleOutputFilesProvided() throws Exception {

    List<String> optionVariations = List.of("--keyout", "-filename");
    List<String> valueVariations = List.of("myfile", "myotherfile", "yetanother");
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);

    valueVariations.forEach(
        filename -> {
          when(keyGenerator.generate(filename, null, null)).thenReturn(configKeyPair);
        });

    when(keyGeneratorFactory.create(refEq(null), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    for (String option : optionVariations) {
      String arg = option.concat("=").concat(String.join(",", valueVariations));

      int exitCode = commandLine.execute(arg);
      assertThat(exitCode).isZero();
      CommandLine.ParseResult parseResult = commandLine.getParseResult();

      assertThat(parseResult).isNotNull();
      assertThat(parseResult.matchedArgs()).hasSize(1);
      assertThat(parseResult.hasMatchedOption(option));
      assertThat(parseResult.unmatched()).isEmpty();

      CliResult result = commandLine.getExecutionResult();
      assertThat(result).isNotNull();
      assertThat(result.isSuppressStartup()).isTrue();
      assertThat(result.getConfig()).isNotPresent();
    }

    verify(keyDataMarshaller, times(optionVariations.size() * valueVariations.size()))
        .marshal(configKeyPair);
    verify(keyGeneratorFactory, times(optionVariations.size()))
        .create(refEq(null), any(EncryptorConfig.class));

    valueVariations.forEach(
        filename -> {
          verify(keyGenerator, times(optionVariations.size())).generate(filename, null, null);
        });
  }

  @Test
  public void noConfigFromKeyGenFileUpdateOptions() throws Exception {

    int exitCode = commandLine.execute("--configout=bogus");
    assertThat(exitCode).isEqualTo(executionExceptionHandler.getExitCode());
    assertThat(executionExceptionHandler.getExceptions()).hasSize(1);

    CliException cliException =
        executionExceptionHandler.getExceptions().stream()
            .filter(CliException.class::isInstance)
            .findFirst()
            .map(CliException.class::cast)
            .get();

    assertThat(cliException).hasMessage("Missing required argument(s): --configfile=<config>");
  }

  @Test
  public void noVaultTypeDefined() {

    int outcome = commandLine.execute("--vault.url=bogus");
    assertThat(outcome).isEqualTo(executionExceptionHandler.getExitCode());
    assertThat(executionExceptionHandler.getExceptions()).hasSize(1);

    CliException cliException =
        executionExceptionHandler.getExceptions().stream()
            .findFirst()
            .map(CliException.class::cast)
            .get();

    assertThat(cliException).hasMessage("Key vault type either not provided or not recognised");
  }

  @Test
  public void nullVaultUrlProvidedOnCommandLine() {
    int outcome = commandLine.execute("--vault.type=AZURE");
    assertThat(outcome).isEqualTo(executionExceptionHandler.getExitCode());
    assertThat(executionExceptionHandler.getExceptions()).hasSize(1);
    ConstraintViolationException constraintViolationException =
        executionExceptionHandler.getExceptions().stream()
            .filter(ConstraintViolationException.class::isInstance)
            .findFirst()
            .map(ConstraintViolationException.class::cast)
            .get();

    assertThat(constraintViolationException).hasMessage("url: may not be null");
  }

  @Test
  public void vaultUrlProvidedOnCommandLine() {

    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(keyGenerator.generate("", null, null)).thenReturn(configKeyPair);

    when(keyGeneratorFactory.create(any(AzureKeyVaultConfig.class), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    int outcome = commandLine.execute("--vault.type=AZURE", "--vault.url=someurl");
    assertThat(outcome).isZero();

    executionExceptionHandler.getExceptions().forEach(Throwable::printStackTrace);

    assertThat(executionExceptionHandler.getExceptions()).isEmpty();
    verify(keyGenerator).generate("", null, null);
    verify(keyGeneratorFactory).create(any(AzureKeyVaultConfig.class), any(EncryptorConfig.class));
    verify(keyDataMarshaller).marshal(configKeyPair);
  }

  @Test
  public void onlyConfigWithKeysProvided() throws Exception {
    // given
    when(keyGeneratorFactory.create(eq(null), any(EncryptorConfig.class))).thenReturn(keyGenerator);

    CommandLine commandLine = new CommandLine(keyGenCommand);

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    CommandLine.ITypeConverter<Config> configConverter = mock(CommandLine.ITypeConverter.class);
    when(configConverter.convert("myconfig.file")).thenReturn(config);

    commandLine.registerConverter(Config.class, configConverter);

    int exceptionExitCode = 999;
    List<Exception> exceptions = new ArrayList<>();
    commandLine.setExecutionExceptionHandler(
        (ex, cmd, parseResult) -> {
          exceptions.add(ex);
          return exceptionExitCode;
        });

    int exitCode = commandLine.execute("--configfile=myconfig.file");

    assertThat(exitCode).isZero();
    assertThat(exceptions).isEmpty();
    verify(configConverter).convert("myconfig.file");

    CliResult result = commandLine.getExecutionResult();
    assertThat(result).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();
    assertThat(result.getStatus()).isZero();

    verifyNoMoreInteractions(configConverter);
    verify(keyGeneratorFactory).create(eq(null), any(EncryptorConfig.class));

    verify(configFileUpdaterWriter).updateAndWriteToCLI(anyList(), eq(null), any(Config.class));

    verify(keyDataMarshaller).marshal(null);

    verify(keyGenerator).generate("", null, null);
  }

  @Test
  public void hashicorpNoKeyOutDefinedRaisesCliException() throws Exception {
    when(keyGeneratorFactory.create(any(), any())).thenReturn(mock(KeyGenerator.class));

    CommandLine commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);

    int result = commandLine.execute("--vault.type=HASHICORP", "--vault.url=someurl");

    assertThat(executionExceptionHandler.getExceptions()).hasSize(1);
    assertThat(result).isEqualTo(executionExceptionHandler.getExitCode());

    CliException cliException =
        executionExceptionHandler.getExceptions().stream()
            .map(CliException.class::cast)
            .findFirst()
            .get();

    assertThat(cliException)
        .hasMessage(
            "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
  }

  @Test
  public void hashicorpNoKeyOutDefinedRaisesCliExceptionEmptyList() throws Exception {
    when(keyGeneratorFactory.create(any(), any())).thenReturn(mock(KeyGenerator.class));

    CommandLine commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);

    int result = commandLine.execute("--vault.type=HASHICORP", "--vault.url=someurl");

    assertThat(executionExceptionHandler.getExceptions()).hasSize(1);
    assertThat(result).isEqualTo(executionExceptionHandler.getExitCode());

    CliException cliException =
        executionExceptionHandler.getExceptions().stream()
            .map(CliException.class::cast)
            .findFirst()
            .get();

    assertThat(cliException)
        .hasMessage(
            "At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
  }

  @Test
  public void hashicorpKeyOutDefinedRaises() throws Exception {
    when(keyGeneratorFactory.create(any(), any())).thenReturn(mock(KeyGenerator.class));

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyData()).thenReturn(List.of(mock(KeyData.class)));
    when(config.getKeys()).thenReturn(keyConfiguration);

    CommandLine commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);
    commandLine.registerConverter(Config.class, value -> config);
    String keyout = "key.out";
    int result =
        commandLine.execute(
            "--vault.type=HASHICORP",
            "--vault.url=someurl",
            "--configfile=".concat(keyout),
            "--keyout=".concat(keyout));

    executionExceptionHandler.getExceptions().forEach(Throwable::printStackTrace);

    assertThat(executionExceptionHandler.getExceptions()).isEmpty();
    assertThat(result).isZero();

    verify(keyGeneratorFactory).create(any(), any());
    verify(configFileUpdaterWriter).updateAndWriteToCLI(anyList(), any(), refEq(config));
    verify(keyDataMarshaller).marshal(any());
  }

  @Test
  public void prepareConfigForNewKeys() {
    Config config = new Config();
    KeyGenCommand.prepareConfigForNewKeys(config);
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getKeys().getKeyData()).isEmpty();
  }

  static class CliExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    private List<Exception> exceptions = new ArrayList<>();

    private int exitCode = 999;

    @Override
    public int handleExecutionException(
        Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult)
        throws Exception {
      exceptions.add(ex);
      return exitCode;
    }

    public List<Exception> getExceptions() {
      return List.copyOf(exceptions);
    }

    public int getExitCode() {
      return exitCode;
    }
  }
}
