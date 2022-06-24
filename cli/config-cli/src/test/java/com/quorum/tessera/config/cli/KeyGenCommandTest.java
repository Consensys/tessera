package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.util.ConfigFileUpdaterWriter;
import com.quorum.tessera.config.util.PasswordFileUpdaterWriter;
import com.quorum.tessera.key.generation.GeneratedKeyPair;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;
import picocli.CommandLine;

@RunWith(MockitoJUnitRunner.class)
public class KeyGenCommandTest {

  @Rule public SystemOutRule systemOutOutput = new SystemOutRule().enableLog();

  @Captor protected ArgumentCaptor<CommandLine.ParameterException> parameterExceptionArgumentCaptor;
  private KeyGeneratorFactory keyGeneratorFactory;
  private ConfigFileUpdaterWriter configFileUpdaterWriter;
  private PasswordFileUpdaterWriter passwordFileUpdaterWriter;
  private KeyDataMarshaller keyDataMarshaller;
  private KeyGenCommand keyGenCommand;
  private KeyGenerator keyGenerator;
  private CliExecutionExceptionHandler executionExceptionHandler;
  private CommandLine.IParameterExceptionHandler parameterExceptionHandler;
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
    parameterExceptionHandler = mock(CLIExceptionCapturer.class);

    commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);
    commandLine.setParameterExceptionHandler(parameterExceptionHandler);
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
    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate("", null, null)).thenReturn(gkp);

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

    verify(keyGeneratorFactory).create(refEq(null), any(EncryptorConfig.class));

    verify(keyGenerator).generate("", null, null);
  }

  @Test
  public void updateNoOutputFileDefined() {
    String filename = "";

    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate(filename, null, null)).thenReturn(gkp);

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
    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(configKeyPair.getPassword()).thenReturn(password);
    when(keyGenerator.generate(filename, null, null)).thenReturn(gkp);

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

    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate("myfile", null, null)).thenReturn(gkp);
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

    verify(keyGeneratorFactory, times(optionVariations.size()))
        .create(refEq(null), any(EncryptorConfig.class));

    verify(keyGenerator, times(optionVariations.size())).generate("myfile", null, null);
  }

  @Test
  public void onlyMulipleOutputFilesProvided() throws Exception {

    List<String> optionVariations = List.of("--keyout", "-filename");
    List<String> valueVariations = List.of("myfile", "myotherfile", "yetanother");
    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);

    valueVariations.forEach(
        filename -> {
          when(keyGenerator.generate(filename, null, null)).thenReturn(gkp);
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

    verify(keyGeneratorFactory, times(optionVariations.size()))
        .create(refEq(null), any(EncryptorConfig.class));

    valueVariations.forEach(
        filename -> {
          verify(keyGenerator, times(optionVariations.size())).generate(filename, null, null);
        });
  }

  @Test
  public void noConfigFromKeyGenFileUpdateOptions() throws Exception {

    // Missing required argument is a parameterException not an executionException
    commandLine.execute("--configout=bogus");

    verify(parameterExceptionHandler)
        .handleParseException(parameterExceptionArgumentCaptor.capture(), any());
    CommandLine.ParameterException parameterException = parameterExceptionArgumentCaptor.getValue();
    assertThat(parameterException)
        .hasMessageContaining("Missing required argument(s): --config-file=<config>");
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

    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate("", null, null)).thenReturn(gkp);

    when(keyGeneratorFactory.create(any(AzureKeyVaultConfig.class), any(EncryptorConfig.class)))
        .thenReturn(keyGenerator);

    int outcome = commandLine.execute("--vault.type=AZURE", "--vault.url=someurl");
    assertThat(outcome).isZero();

    executionExceptionHandler.getExceptions().forEach(Throwable::printStackTrace);

    assertThat(executionExceptionHandler.getExceptions()).isEmpty();
    verify(keyGenerator).generate("", null, null);
    verify(keyGeneratorFactory).create(any(AzureKeyVaultConfig.class), any(EncryptorConfig.class));
  }

  @Test
  public void onlyConfigWithKeysProvided() throws Exception {
    // given
    when(keyGeneratorFactory.create(eq(null), any(EncryptorConfig.class))).thenReturn(keyGenerator);

    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate("", null, null)).thenReturn(gkp);

    CommandLine commandLine = new CommandLine(keyGenCommand);

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    CommandLine.ITypeConverter<Config> configConverter = mock(CommandLine.ITypeConverter.class);
    when(configConverter.convert("myconfig.file")).thenReturn(config);

    commandLine.registerConverter(Config.class, configConverter);

    int exitCode = commandLine.execute("--configfile=myconfig.file");

    assertThat(exitCode).isZero();
    verify(configConverter).convert("myconfig.file");

    CliResult result = commandLine.getExecutionResult();
    assertThat(result).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();
    assertThat(result.getStatus()).isZero();

    verifyNoMoreInteractions(configConverter);
    verify(keyGeneratorFactory).create(eq(null), any(EncryptorConfig.class));

    verify(configFileUpdaterWriter).updateAndWriteToCLI(anyList(), eq(null), any(Config.class));

    verify(keyDataMarshaller).marshal(configKeyPair);

    verify(keyGenerator).generate("", null, null);
  }

  @Test
  public void hashicorpNoKeyOutDefinedRaisesCliException() throws Exception {
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
    when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

    String keyout = "key.out";

    GeneratedKeyPair gkp = mock(GeneratedKeyPair.class);
    ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
    when(gkp.getConfigKeyPair()).thenReturn(configKeyPair);
    when(keyGenerator.generate(keyout, null, null)).thenReturn(gkp);

    Config config = mock(Config.class);
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyData()).thenReturn(List.of(mock(KeyData.class)));
    when(config.getKeys()).thenReturn(keyConfiguration);

    CommandLine commandLine = new CommandLine(keyGenCommand);
    commandLine.setExecutionExceptionHandler(executionExceptionHandler);
    commandLine.registerConverter(Config.class, value -> config);
    int result =
        commandLine.execute(
            "--vault.type=HASHICORP",
            "--vault.url=someurl",
            "--configfile=".concat("myconfig.json"),
            "--keyout=".concat(keyout));

    executionExceptionHandler.getExceptions().forEach(Throwable::printStackTrace);

    assertThat(executionExceptionHandler.getExceptions()).isEmpty();
    assertThat(result).isZero();

    verify(keyGeneratorFactory).create(any(), any());
    verify(keyGenerator).generate(keyout, null, null);
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

  @Test
  public void output() {
    FilesystemKeyPair file = mock(FilesystemKeyPair.class);
    when(file.getPublicKeyPath()).thenReturn(Paths.get("/file.pub"));
    when(file.getPrivateKeyPath()).thenReturn(Paths.get("/file.key"));

    HashicorpVaultKeyPair hashi = mock(HashicorpVaultKeyPair.class);
    when(hashi.getSecretEngineName()).thenReturn("kv");
    when(hashi.getSecretName()).thenReturn("mySecret");
    when(hashi.getPublicKeyId()).thenReturn("publicKey");
    when(hashi.getPrivateKeyId()).thenReturn("privateKey");
    when(hashi.getSecretVersion()).thenReturn(1);

    AzureVaultKeyPair azure = mock(AzureVaultKeyPair.class);
    when(azure.getPublicKeyId()).thenReturn("myPub");
    when(azure.getPrivateKeyId()).thenReturn("myPriv");
    when(azure.getPublicKeyVersion()).thenReturn("abc123");
    when(azure.getPrivateKeyVersion()).thenReturn("def456");

    AWSKeyPair aws = mock(AWSKeyPair.class);
    when(aws.getPublicKeyId()).thenReturn("myPub");
    when(aws.getPrivateKeyId()).thenReturn("myPriv");

    // cover cases where a new key pair gets implemented and output code is not yet updated
    UnknownKeyPair unknown = mock(UnknownKeyPair.class);
    when(unknown.getPublicKey()).thenReturn("unknownPub");

    List<GeneratedKeyPair> kps =
        List.of(
            new GeneratedKeyPair(file, "filePub"),
            new GeneratedKeyPair(hashi, "hashiPub"),
            new GeneratedKeyPair(azure, "azurePub"),
            new GeneratedKeyPair(aws, "awsPub"),
            new GeneratedKeyPair(unknown, "unknownPub"));
    KeyGenCommand.output(kps);

    String got = systemOutOutput.getLog();

    StringJoiner sj = new StringJoiner("\n");
    sj.add("5 keypair(s) generated:");

    sj.add("\t1: type=file, pub=filePub");
    sj.add("\t\tpub: path=/file.pub");
    sj.add("\t\tprv: path=/file.key");

    sj.add("\t2: type=hashicorp, pub=hashiPub");
    sj.add("\t\tpub: name=kv/mySecret, id=publicKey, version=1");
    sj.add("\t\tprv: name=kv/mySecret, id=privateKey, version=1");

    sj.add("\t3: type=azure, pub=azurePub");
    sj.add("\t\tpub: id=myPub, version=abc123");
    sj.add("\t\tprv: id=myPriv, version=def456");

    sj.add("\t4: type=aws, pub=awsPub");
    sj.add("\t\tpub: id=myPub");
    sj.add("\t\tprv: id=myPriv");

    sj.add("\t5: type=unknown, pub=unknownPub");

    String expected = sj.toString();

    assertThat(got).contains(expected);
  }

  static class UnknownKeyPair implements ConfigKeyPair {

    @Override
    public String getPublicKey() {
      return null;
    }

    @Override
    public String getPrivateKey() {
      return null;
    }

    @Override
    public void withPassword(char[] password) {}

    @Override
    public char[] getPassword() {
      return new char[0];
    }
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
