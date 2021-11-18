package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PicoCliDelegateTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegateTest.class);

  private PicoCliDelegate cliDelegate;

  @Rule public SystemErrRule systemErrOutput = new SystemErrRule().enableLog();

  @Rule public SystemOutRule systemOutOutput = new SystemOutRule().enableLog();

  private MockedStatic<KeyGeneratorFactory> keyGeneratorFactoryFunction;

  private KeyGeneratorFactory keyGeneratorFactory;

  private KeyGenerator keyGenerator;

  @Before
  public void beforeTest() {

    keyGeneratorFactory = mock(KeyGeneratorFactory.class);
    keyGeneratorFactoryFunction = mockStatic(KeyGeneratorFactory.class);
    keyGenerator = mock(KeyGenerator.class);

    when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);
    keyGeneratorFactoryFunction.when(KeyGeneratorFactory::create).thenReturn(keyGeneratorFactory);

    cliDelegate = new PicoCliDelegate();
    this.systemErrOutput.clearLog();
    this.systemOutOutput.clearLog();
  }

  @After
  public void afterTest() {
    keyGeneratorFactoryFunction.close();
  }

  @Test
  public void help() throws Exception {
    final CliResult result = cliDelegate.execute("help");

    final String sysout = systemOutOutput.getLog();
    final String syserr = systemErrOutput.getLog();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(syserr).isEmpty();
    assertThat(sysout).isNotEmpty();
    assertThat(sysout)
        .contains("Usage: tessera [OPTIONS] [COMMAND]", "Description:", "Options:", "Commands:");
  }

  @Test
  public void noArgsPrintsHelp() throws Exception {
    final CliResult result = cliDelegate.execute();

    final String sysout = systemOutOutput.getLog();
    final String syserr = systemErrOutput.getLog();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(syserr).isEmpty();
    assertThat(sysout).isNotEmpty();
    assertThat(sysout)
        .contains("Usage: tessera [OPTIONS] [COMMAND]", "Description:", "Options:", "Commands:");
  }

  @Test
  public void keygenHelp() throws Exception {
    final CliResult result = cliDelegate.execute("keygen", "help");

    final String sysout = systemOutOutput.getLog();
    final String syserr = systemErrOutput.getLog();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(syserr).isEmpty();
    assertThat(sysout).isNotEmpty();
    assertThat(sysout)
        .contains(
            "Usage: tessera keygen [OPTIONS] [COMMAND]", "Description:", "Options:", "Commands:");
  }

  @Test
  public void keygenNoArgsPrintsHelp() throws Exception {
    final CliResult result = cliDelegate.execute("keygen");

    final String sysout = systemOutOutput.getLog();
    final String syserr = systemErrOutput.getLog();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(syserr).isEmpty();
    assertThat(sysout).isNotEmpty();
    assertThat(sysout)
        .contains(
            "Usage: tessera keygen [OPTIONS] [COMMAND]", "Description:", "Options:", "Commands:");
  }

  @Test
  public void keyupdateHelp() throws Exception {
    final CliResult result = cliDelegate.execute("keyupdate", "help");

    final String sysout = systemOutOutput.getLog();
    final String syserr = systemErrOutput.getLog();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isNotPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(syserr).isEmpty();
    assertThat(sysout).isNotEmpty();
    assertThat(sysout)
        .contains(
            "Usage: tessera keyupdate [OPTIONS] [COMMAND]",
            "Description:",
            "Options:",
            "Commands:");
  }

  @Test
  public void keyupdateNoArgsErrorsAndPrintsHelp() {
    Throwable ex = catchThrowable(() -> cliDelegate.execute("keyupdate"));

    final String syserr = systemErrOutput.getLog();

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage("Missing required option '--keys.keyData.privateKeyPath <privateKeyPath>'");

    assertThat(syserr).isNotEmpty();
    assertThat(syserr)
        .contains(
            "Usage: tessera keyupdate [OPTIONS] [COMMAND]",
            "Description:",
            "Options:",
            "Commands:");
  }

  @Test
  public void withValidConfig() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    CliResult result = cliDelegate.execute("-configfile", configFile.toString());

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isFalse();
  }

  @Test
  public void withValidConfigAndPidfile() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    String tempDir = System.getProperty("java.io.tmpdir");
    Path pidFilePath = Paths.get(tempDir, UUID.randomUUID().toString());

    assertThat(pidFilePath).doesNotExist();

    CliResult result =
        cliDelegate.execute(
            "-configfile", configFile.toString(), "-pidfile", pidFilePath.toString());

    assertThat(pidFilePath).exists();
    pidFilePath.toFile().deleteOnExit();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isFalse();
  }

  @Test
  public void withValidConfigAndPidfileAlreadyExists() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    Path pidFilePath = Files.createTempFile(UUID.randomUUID().toString(), "");
    pidFilePath.toFile().deleteOnExit();

    assertThat(pidFilePath).exists();

    CliResult result =
        cliDelegate.execute(
            "-configfile", configFile.toString(), "-pidfile", pidFilePath.toString());

    assertThat(pidFilePath).exists();

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isFalse();
  }

  @Test
  public void processArgsMissing() throws Exception {
    Throwable ex = catchThrowable(() -> cliDelegate.execute("-configfile"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex).hasMessage("Missing required parameter for option '--config-file' (<config>)");
  }

  @Test
  public void withConstraintViolations() throws Exception {
    Path configFile = Paths.get(getClass().getResource("/missing-config.json").toURI());
    try {
      cliDelegate.execute("-configfile", configFile.toString());
      failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
    } catch (ConstraintViolationException ex) {
      assertThat(ex.getConstraintViolations()).isNotEmpty();
    }
  }

  @Test
  public void keygen() throws Exception {

    FilesystemKeyPair keypair = mock(FilesystemKeyPair.class);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    CliResult result = cliDelegate.execute("-keygen", "-filename", UUID.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.getConfig()).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();

    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
  }

  @Test
  public void keygenThenExit() throws Exception {
    FilesystemKeyPair keypair = mock(FilesystemKeyPair.class);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    final CliResult result = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

    assertThat(result).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();
    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
  }

  @Test
  public void noConfigfileOption() {

    final Throwable throwable = catchThrowable(() -> cliDelegate.execute("--pidfile", "bogus"));
    assertThat(throwable)
        .isInstanceOf(CliException.class)
        .hasMessage("Missing required option '--configfile <config>'");
  }

  @Test
  public void keygenUpdateConfig() throws Exception {

    Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
    Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

    Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
    Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

    FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    Path configFile = Paths.get(getClass().getResource("/keygen-sample.json").toURI());
    Path keyOutputPath = configFile.resolveSibling(UUID.randomUUID().toString());
    Path configOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".json");

    assertThat(Files.exists(configOutputPath)).isFalse();

    CliResult result =
        cliDelegate.execute(
            "-keygen",
            "-filename",
            keyOutputPath.toString(),
            "-output",
            configOutputPath.toString(),
            "-configfile",
            configFile.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.getConfig()).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(Files.exists(configOutputPath)).isTrue();
    configOutputPath.toFile().deleteOnExit();

    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
    verifyNoMoreInteractions(keyGenerator);

    try {
      cliDelegate.execute(
          "-keygen",
          "-filename",
          UUID.randomUUID().toString(),
          "-output",
          configOutputPath.toString(),
          "-configfile",
          configFile.toString());
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(UncheckedIOException.class);
      assertThat(ex.getCause()).isExactlyInstanceOf(FileAlreadyExistsException.class);
    }
  }

  @Test
  public void keygenUpdateConfigAndPasswordFile() throws Exception {

    Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
    Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

    Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
    Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

    FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    Path configFile = Paths.get(getClass().getResource("/keygen-sample.json").toURI());
    Path keyOutputPath = configFile.resolveSibling(UUID.randomUUID().toString());
    Path configOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".json");
    Path pwdOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".pwds");

    assertThat(Files.exists(configOutputPath)).isFalse();
    assertThat(Files.exists(pwdOutputPath)).isFalse();

    CliResult result =
        cliDelegate.execute(
            "-keygen",
            "-filename",
            keyOutputPath.toString(),
            "-output",
            configOutputPath.toString(),
            "-configfile",
            configFile.toString(),
            "--pwdout",
            pwdOutputPath.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.getConfig()).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();

    assertThat(Files.exists(configOutputPath)).isTrue();
    configOutputPath.toFile().deleteOnExit();

    assertThat(Files.exists(pwdOutputPath)).isTrue();
    pwdOutputPath.toFile().deleteOnExit();

    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
    verifyNoMoreInteractions(keyGenerator);

    try {
      cliDelegate.execute(
          "-keygen",
          "-filename",
          UUID.randomUUID().toString(),
          "-output",
          configOutputPath.toString(),
          "-configfile",
          configFile.toString());
      failBecauseExceptionWasNotThrown(Exception.class);
    } catch (Exception ex) {
      assertThat(ex).isInstanceOf(UncheckedIOException.class);
      assertThat(ex.getCause()).isExactlyInstanceOf(FileAlreadyExistsException.class);
    }
  }

  @Test
  public void keygenOutputToCLI() throws Exception {

    Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
    Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

    Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
    Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

    FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    Path configFile = Paths.get(getClass().getResource("/keygen-sample.json").toURI());
    Path keyOutputPath = configFile.resolveSibling(UUID.randomUUID().toString());

    CliResult result =
        cliDelegate.execute(
            "-keygen", "-filename", keyOutputPath.toString(), "-configfile", configFile.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.getConfig()).isNotNull();
    assertThat(result.isSuppressStartup()).isTrue();

    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
    verifyNoMoreInteractions(keyGenerator);
  }

  //    @Test
  //    public void keygenFileUpdateOptionsRequireConfigfile() {
  //
  //        FilesystemKeyPair keypair = mock(FilesystemKeyPair.class);
  //        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);
  //
  //        Throwable ex = catchThrowable(() -> cliDelegate.execute("-keygen", "-output",
  // "somepath"));
  //
  //        assertThat(ex).isNotNull();
  //        assertThat(ex).isExactlyInstanceOf(CliException.class);
  //        assertThat(ex.getMessage()).contains("Missing required argument(s):
  // --configfile=<config>");
  //    }

  @Test
  public void configOverride() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    CliResult result =
        cliDelegate.execute(
            "-configfile",
            configFile.toString(),
            "-o",
            "jdbc.autoCreateTables=true",
            "-o",
            "useWhiteList=true");

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getConfig().get().getJdbcConfig().isAutoCreateTables()).isTrue();
    assertThat(result.getConfig().get().isUseWhiteList()).isTrue();
  }

  @Test
  public void configOverrideNoParameter() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(() -> cliDelegate.execute("-configfile", configFile.toString(), "-o"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage("Missing required parameter for option '--override' (<String=String>)");
  }

  @Test
  public void configOverrideNoTarget() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(
            () -> cliDelegate.execute("-configfile", configFile.toString(), "-o", "=true"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex).hasMessage("java.lang.NoSuchFieldException: ");
  }

  @Test
  public void configOverrideNoValue() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(
            () ->
                cliDelegate.execute(
                    "-configfile", configFile.toString(), "-o", "jdbc.autoCreateTables"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage(
            "Value for option option '--override' (<String=String>) should be in KEY=VALUE format but was jdbc.autoCreateTables");
  }

  @Test
  public void configOverrideUnknownTarget() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    Throwable ex =
        catchThrowable(
            () ->
                cliDelegate.execute(
                    "-configfile", configFile.toString(), "-o", "bogus=bogusvalue"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex).hasMessage("java.lang.NoSuchFieldException: bogus");
  }

  @Test
  public void overrideAlwaysSendTo() throws Exception {

    String alwaysSendToKey = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    CliResult result = null;
    try {
      result =
          cliDelegate.execute(
              "-configfile",
              configFile.toString(),
              "-o",
              Strings.join("alwaysSendTo[1]=", alwaysSendToKey).with(""));
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getConfig().get().getAlwaysSendTo()).hasSize(2);
    assertThat(result.getConfig().get().getAlwaysSendTo())
        .containsExactly("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", alwaysSendToKey);
  }

  @Test
  public void overridePeers() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    CliResult result =
        cliDelegate.execute(
            "-configfile", configFile.toString(),
            "-o", "peer[2].url=http://anotherpeer",
            "--override", "peer[3].url=http://yetanotherpeer");

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getConfig().get().getPeers()).hasSize(4);
    assertThat(result.getConfig().get().getPeers().stream().map(Peer::getUrl))
        .containsExactlyInAnyOrder(
            "http://anotherpeer",
            "http://yetanotherpeer",
            "http://bogus1.com",
            "http://bogus2.com");
  }

  @Test
  public void legacyConfigOverride() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    CliResult result =
        cliDelegate.execute(
            "-configfile",
            configFile.toString(),
            "-jdbc.autoCreateTables",
            "true",
            "-useWhiteList",
            "true");

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getStatus()).isEqualTo(0);
    assertThat(result.isSuppressStartup()).isFalse();

    Config config = result.getConfig().get();
    assertThat(config.getJdbcConfig()).isNotNull();
    assertThat(config.getJdbcConfig().isAutoCreateTables()).isTrue();
    assertThat(config.isUseWhiteList()).isTrue();
  }

  @Test
  public void legacyConfigOverrideNoTarget() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(() -> cliDelegate.execute("-configfile", configFile.toString(), "true"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage("Invalid config overrides. Consider using the --override option instead.");
  }

  @Test
  public void legacyConfigOverrideNoValue() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(
            () ->
                cliDelegate.execute(
                    "-configfile", configFile.toString(), "-jdbc.autoCreateTables"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage("Invalid config overrides. Consider using the --override option instead.");
  }

  @Test
  public void legacyConfigOverrideSomeNoValue() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    Throwable ex =
        catchThrowable(
            () ->
                cliDelegate.execute(
                    "-configfile",
                    configFile.toString(),
                    "-jdbc.autoCreateTables",
                    "true",
                    "-useWhiteList"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage("Invalid config overrides. Consider using the --override option instead.");
  }

  @Test
  public void legacyConfigOverrideUnknownTarget() throws Exception {

    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    Throwable ex =
        catchThrowable(
            () ->
                cliDelegate.execute("-configfile", configFile.toString(), "-bogus", "bogusvalue"));

    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage(
            String.join(
                "\n",
                "Invalid config overrides. Consider using the --override option instead.",
                "java.lang.NoSuchFieldException: bogus"));
  }

  @Test
  public void updatingPasswordsDoesntProcessOtherOptions() throws Exception {

    final InputStream oldIn = System.in;
    final InputStream inputStream =
        new ByteArrayInputStream((System.lineSeparator() + System.lineSeparator()).getBytes());
    System.setIn(inputStream);

    final KeyDataConfig startingKey =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

    final Path key = Files.createTempFile("key", ".key");
    Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

    final CliResult result =
        cliDelegate.execute(
            "-updatepassword",
            "--keys.keyData.privateKeyPath",
            key.toString(),
            "--keys.passwords",
            "testpassword");

    assertThat(result).isNotNull();

    System.setIn(oldIn);
  }

  @Test
  public void suppressStartupForKeygenOption() throws Exception {

    when(keyGenerator.generate(anyString(), eq(null), eq(null)))
        .thenReturn(mock(DirectKeyPair.class));

    final CliResult cliResult = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

    assertThat(cliResult.isSuppressStartup()).isTrue();

    verify(keyGenerator).generate(anyString(), eq(null), eq(null));
  }

  @Test
  public void suppressStartupForKeygenOptionWithFileOutputOptions() throws Exception {

    Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
    Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

    Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
    Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

    FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);
    when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

    final Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

    final Path configOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".json");

    final CliResult cliResult =
        cliDelegate.execute(
            "-keygen",
            "-configfile",
            configFile.toString(),
            "-output",
            configOutputPath.toString());

    assertThat(cliResult.isSuppressStartup()).isTrue();
  }

  @Test
  public void subcommandExceptionIsThrown() {
    Throwable ex =
        catchThrowable(
            () -> cliDelegate.execute("-keygen", "-keygenvaulturl", "urlButNoVaultType"));

    assertThat(ex).isNotNull();
    assertThat(ex).isInstanceOf(CliException.class);
  }

  @Test
  public void withRecoverMode() throws Exception {
    Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());
    CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-r");

    assertThat(result).isNotNull();
    assertThat(result.getConfig()).isPresent();
    assertThat(result.getStatus()).isEqualTo(0);

    Config config = result.getConfig().get();
    assertThat(config.isRecoveryMode()).isTrue();
  }
}
