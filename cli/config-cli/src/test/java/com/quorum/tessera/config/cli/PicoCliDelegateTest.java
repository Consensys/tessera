package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import org.assertj.core.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PicoCliDelegateTest {

    @Rule
    public TemporaryFolder dir = new TemporaryFolder();

    private Path baseDir;

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegateTest.class);

    private PicoCliDelegate cliDelegate;

    private KeyPasswordResolver keyPasswordResolver;

    private MockedStatic<KeyGeneratorFactory> keyGeneratorFactoryMockedStatic;

    private KeyGenerator keyGenerator;

    private MockedStatic<KeyDataMarshaller> keyDataMarshallerMockedStatic;

    private KeyGeneratorFactory keyGeneratorFactory;

    private KeyGenCommand keyGenCommand;

    private KeyUpdateCommand keyUpdateCommand;

    private KeyDataMarshaller keyDataMarshaller;

    @Before
    public void beforeTest() throws Exception {
        baseDir = dir.getRoot().toPath();

        keyGenCommand = mock(KeyGenCommand.class);
        keyUpdateCommand = mock(KeyUpdateCommand.class);
        keyGeneratorFactory = mock(KeyGeneratorFactory.class);
        keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        keyDataMarshaller = mock(KeyDataMarshaller.class);

        keyGeneratorFactoryMockedStatic = mockStatic(KeyGeneratorFactory.class);
        keyGeneratorFactoryMockedStatic.when(KeyGeneratorFactory::create).thenReturn(keyGeneratorFactory);

        keyDataMarshallerMockedStatic = mockStatic(KeyDataMarshaller.class);
        keyDataMarshallerMockedStatic.when(KeyDataMarshaller::create).thenReturn(keyDataMarshaller);

        keyPasswordResolver = mock(KeyPasswordResolver.class);

        cliDelegate = new PicoCliDelegate(keyPasswordResolver);
    }

    @After
    public void afterTest() {

        try {
            verifyNoMoreInteractions(keyDataMarshaller);
            verifyNoMoreInteractions(keyGeneratorFactory);
            verifyNoMoreInteractions(keyGenerator);
            verifyNoMoreInteractions(keyPasswordResolver);
            verifyNoMoreInteractions(keyGenCommand);
            verifyNoMoreInteractions(keyUpdateCommand);
        } finally {
            keyGeneratorFactoryMockedStatic.close();
            keyDataMarshallerMockedStatic.close();
        }

    }

    @Test
    public void help() throws Exception {

        final CliResult result = cliDelegate.execute("help");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void noArgsPrintsHelp() throws Exception {

        final CliResult result = cliDelegate.execute();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void subcommandWithNoArgsPrintsHelp() throws Exception {

        final CliResult result = cliDelegate.execute("keygen");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();
    }

    @Test
    public void withValidConfig() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());

        CliResult result = cliDelegate.execute("-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test
    public void withValidConfigAndPidfile() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());

        String tempDir = System.getProperty("java.io.tmpdir");
        Path pidFilePath = Paths.get(tempDir, UUID.randomUUID().toString());

        assertThat(pidFilePath).doesNotExist();

        CliResult result =
            cliDelegate.execute("-configfile", configFile.toString(), "-pidfile", pidFilePath.toString());

        assertThat(pidFilePath).exists();
        pidFilePath.toFile().deleteOnExit();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test
    public void withValidConfigAndPidfileAlreadyExists() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());

        Path pidFilePath = Files.createTempFile(UUID.randomUUID().toString(), "");
        pidFilePath.toFile().deleteOnExit();

        assertThat(pidFilePath).exists();

        CliResult result =
            cliDelegate.execute("-configfile", configFile.toString(), "-pidfile", pidFilePath.toString());

        assertThat(pidFilePath).exists();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        cliDelegate.execute("-configfile");
    }

    @Test
    public void withConstraintViolations() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/missing-config.json").toURI());
        try {
            cliDelegate.execute("-configfile", configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).isNotEmpty();
        }
    }

    @Test
    public void keygen() throws Exception {
        String outputFileName = UUID.randomUUID().toString();
        FilesystemKeyPair keypair = mock(FilesystemKeyPair.class);
        when(keyGenerator.generate(outputFileName, null, null)).thenReturn(keypair);
        CliResult result = cliDelegate.execute("-keygen", "-filename", outputFileName);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isZero();
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isSuppressStartup()).isTrue();

        verify(keyGenerator).generate(outputFileName, null, null);
        verifyNoMoreInteractions(keyGenerator);

        verify(keyDataMarshaller).marshal(keypair);

        verify(keyGeneratorFactory).create(any(), any());
    }

    @Test
    public void keygenThenExit() throws Exception {

        ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
        when(keyGenerator.generate("", null, null)).thenReturn(configKeyPair);

        ArgumentCaptor<ConfigKeyPair> configKeyPairCaptor = ArgumentCaptor.forClass(ConfigKeyPair.class);

        final CliResult result = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

        assertThat(result).isNotNull();
        assertThat(result.isSuppressStartup()).isTrue();

        verify(keyDataMarshaller).marshal(configKeyPairCaptor.capture());
        ConfigKeyPair keyPair = configKeyPairCaptor.getValue();
        assertThat(keyPair).isSameAs(configKeyPair);

        verify(keyGeneratorFactory).create(eq(null), any(EncryptorConfig.class));
        verify(keyGenerator).generate("", null, null);


    }

    @Test
    public void noConfigfileOption() {

        final Throwable throwable = catchThrowable(() -> cliDelegate.execute("--pidfile", "bogus"));
        assertThat(throwable)
            .isInstanceOf(CliException.class)
            .hasMessage("Missing required option '--configfile <config>'");
    }

    @Test
    public void output() throws Exception {

        KeyData keyData = new KeyData();
        keyData.setPublicKey("PublicKeyData");
        keyData.setPrivateKey("PrivateKeyData");

        when(keyDataMarshaller.marshal(any(FilesystemKeyPair.class))).thenReturn(keyData);

        Path publicKeyPath = createSamplePublicKey(baseDir);
        Path privateKeyPath = createSamplePrivateKey(baseDir);

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
        when(keyGenerator.generate(anyString(), any(), any())).thenReturn(keypair);

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



        try {
            cliDelegate.execute(
                "-keygen",
                "-filename",
                UUID.randomUUID().toString(),
                "-output",
                configOutputPath.toString(),
                "-configfile",
                configFile.toString());
            failBecauseExceptionWasNotThrown(UncheckedIOException.class);
        } catch (UncheckedIOException ex) {
            assertThat(ex.getCause()).isExactlyInstanceOf(FileAlreadyExistsException.class);
        }

        verify(keyDataMarshaller,times(2)).marshal(any(ConfigKeyPair.class));
        verify(keyGeneratorFactory,times(2)).create(eq(null),any(EncryptorConfig.class));
        verify(keyGenerator,times(2)).generate(anyString(), eq(null), eq(null));
    }

    @Test
    public void outputConfigAndPasswordFiles() throws Exception {

        KeyData keyData = new KeyData();
        keyData.setPublicKey("PublicKeyData");
        keyData.setPrivateKey("PrivateKeyData");

        when(keyDataMarshaller.marshal(any(FilesystemKeyPair.class))).thenReturn(keyData);

        Path publicKeyPath = createSamplePublicKey(baseDir);
        Path privateKeyPath = createSamplePrivateKey(baseDir);

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
            failBecauseExceptionWasNotThrown(UncheckedIOException.class);
        } catch (UncheckedIOException ex) {
            assertThat(ex.getCause()).isExactlyInstanceOf(FileAlreadyExistsException.class);
        }

        verify(keyDataMarshaller,times(2)).marshal(any(ConfigKeyPair.class));
        verify(keyGeneratorFactory,times(2)).create(eq(null),any(EncryptorConfig.class));
        verify(keyGenerator,times(2)).generate(anyString(), eq(null), eq(null));
    }

    @Test
    public void keygenOutputToCLI() throws Exception {

        KeyData keyData = new KeyData();
        keyData.setPublicKey("PublicKeyData");
        keyData.setPrivateKey("PrivateKeyData");

        when(keyDataMarshaller.marshal(any(FilesystemKeyPair.class))).thenReturn(keyData);

        Map.Entry<Path, Path> sampleKeyPair = createSampleKeyPair(baseDir);
        Path publicKeyPath = sampleKeyPair.getKey();
        Path privateKeyPath = sampleKeyPair.getValue();

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        Path unixSocketPath = baseDir.resolve(UUID.randomUUID().toString().concat(".ipc"));
        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        Path configFile = Paths.get(getClass().getResource("/keygen-sample.json").toURI());
        Path keyOutputPath = configFile.resolveSibling(UUID.randomUUID().toString());

        CliResult result =
            cliDelegate.execute(
                "-keygen", "-filename", keyOutputPath.toString(), "-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isTrue();

        verify(keyGenerator).generate(anyString(), eq(null), eq(null));

        verify(keyDataMarshaller).marshal(keypair);

        verify(keyGeneratorFactory).create(eq(null), any(EncryptorConfig.class));

    }

    @Test
    public void keygenFileUpdateOptionsRequireConfigfile() {
        Throwable ex = catchThrowable(() -> cliDelegate.execute("-keygen", "-output", "somepath"));

        assertThat(ex).isNotNull();
        assertThat(ex).isExactlyInstanceOf(CliException.class);
        assertThat(ex.getMessage()).contains("Missing required argument(s): --configfile=<config>");
    }

    @Test
    public void dynOption() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-o", "jdbc.username=somename");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getJdbcConfig().getUsername()).isEqualTo("somename");
        assertThat(result.getConfig().get().getJdbcConfig().getPassword()).isEqualTo("tiger");

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());

    }

//    @Ignore
//    @Test
//    public void withInvalidPath() throws Exception {
//        // unixSocketPath
//        Map<String, Object> params = new HashMap<>();
//        params.put("publicKeyPath", "BOGUS.bogus");
//        params.put("privateKeyPath", "BOGUS.bogus");
//
//        Path configFile =
//                ElUtil.createTempFileFromTemplate(getClass().getResource("/sample-config-invalidpath.json"), params);
//
//
//
//
//        try {
//            cliDelegate.execute("-configfile", configFile.toString());
//            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
//        } catch (ConstraintViolationException ex) {
//            assertThat(ex.getConstraintViolations())
//                    .hasSize(2)
//                    .extracting("messageTemplate")
//                    .containsExactly("File does not exist", "File does not exist");
//        }
//    }

    @Test
    public void withEmptyConfigOverrideAll() throws Exception {

        Path unixSocketFile = Files.createTempFile("unixSocketFile", ".ipc");
        unixSocketFile.toFile().deleteOnExit();

        Path configFile = Files.createTempFile("withEmptyConfigOverrideAll", ".json");
        configFile.toFile().deleteOnExit();
        Files.write(configFile, "{}".getBytes());
        try {
            CliResult result =
                cliDelegate.execute(
                    "-configfile",
                    configFile.toString(),
                    "-o",
                    Strings.join("unixSocketFile=", unixSocketFile.toString()).with(""),
                    "-o",
                    "encryptor.type=NACL");

            assertThat(result).isNotNull();
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            ex.getConstraintViolations().forEach(v -> LOGGER.info("{}", v));
        }
    }

    @Test
    public void overrideAlwaysSendTo() throws Exception {

        String alwaysSendToKey = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
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

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test
    public void overridePeers() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());

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
                "http://anotherpeer", "http://yetanotherpeer", "http://bogus1.com", "http://bogus2.com");

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    //   @Test
//    public void updatingPasswordsDoesntProcessOtherOptions() throws Exception {
//
//        final InputStream oldIn = System.in;
//        final InputStream inputStream =
//                new ByteArrayInputStream((System.lineSeparator() + System.lineSeparator()).getBytes());
//        System.setIn(inputStream);
//
//        final KeyDataConfig startingKey =
//                JaxbUtil.unmarshal(getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);
//
//        final Path key = Files.createTempFile("key", ".key");
//        Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());
//
//        final CliResult result =
//                cliDelegate.execute(
//                        "-updatepassword",
//                        "--keys.keyData.privateKeyPath",
//                        key.toString(),
//                        "--keys.passwords",
//                        "testpassword");
//
//        assertThat(result).isNotNull();
//
//        System.setIn(oldIn);
//    }

    @Test
    public void suppressStartupForKeygenOption() throws Exception {
        final CliResult cliResult = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

        assertThat(cliResult.isSuppressStartup()).isTrue();
        verify(keyDataMarshaller).marshal(null);//TODO Why shoud we allow this?
        verify(keyGeneratorFactory).create(eq(null),any(EncryptorConfig.class));
        verify(keyGenerator).generate(anyString(),any(),any());
    }

    @Test
    public void suppressStartupForKeygenOptionWithFileOutputOptions() throws Exception {
        KeyData keyData = new KeyData();
        keyData.setPublicKey("PublicKeyData");
        keyData.setPrivateKey("PrivateKeyData");

        when(keyDataMarshaller.marshal(any(FilesystemKeyPair.class))).thenReturn(keyData);

        var keyPair = createSampleKeyPair(baseDir);
        Path publicKeyPath = keyPair.getKey();
        Path privateKeyPath = keyPair.getValue();

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        final Path configFile = Paths.get(getClass().getResource("/sample-config.json").toURI());

        final Path configOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".json");

        final CliResult cliResult =
            cliDelegate.execute(
                "-keygen", "-configfile", configFile.toString(), "-output", configOutputPath.toString());

        assertThat(cliResult.isSuppressStartup()).isTrue();

        verify(keyDataMarshaller).marshal(any(ConfigKeyPair.class));
        verify(keyGeneratorFactory).create(eq(null),any(EncryptorConfig.class));
        verify(keyGenerator).generate(anyString(),any(),any());
    }

    @Test
    public void subcommandExceptionIsThrown() {
        Throwable ex = catchThrowable(() -> cliDelegate.execute("-keygen", "-keygenvaulturl", "urlButNoVaultType"));

        assertThat(ex).isNotNull();
        assertThat(ex).isInstanceOf(CliException.class);
    }

    @Test
    public void withValidConfigAndJdbcOveride() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-jdbc.autoCreateTables", "true");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        Config config = result.getConfig().get();
        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().isAutoCreateTables()).isTrue();

        verify(keyPasswordResolver).resolveKeyPasswords(config);
    }

    @Test
    public void withValidConfigAndUnmatchableDynamicOption() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-bogus");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test
    public void withValidConfigAndUnmatchableDynamicOptionWithValue() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-bogus", "bogus value");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        verify(keyPasswordResolver).resolveKeyPasswords(result.getConfig().get());
    }

    @Test
    public void withValidConfigAndJdbcOverides() throws Exception {

        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result =
            cliDelegate.execute(
                "-configfile", configFile.toString(), "-jdbc.autoCreateTables", "true", "-jdbc.url", "someurl");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        Config config = result.getConfig().get();
        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().isAutoCreateTables()).isTrue();
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("someurl");

        verify(keyPasswordResolver).resolveKeyPasswords(config);
    }

    @Test
    public void withRecoverMode() throws Exception {
        Path configFile = Paths.get(getClass()
            .getResource("/sample-config.json").toURI());
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-r");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        Config config = result.getConfig().get();
        assertThat(config.isRecoveryMode()).isTrue();

        verify(keyPasswordResolver).resolveKeyPasswords(config);
    }

    private Map.Entry<Path, Path> createSampleKeyPair(Path inDir) {
        return createSampleKeyPair(inDir,UUID.randomUUID().toString());
    }

    private Map.Entry<Path, Path> createSampleKeyPair(Path inDir, String name) {
        Path privateKey = createSamplePrivateKey(inDir, name);
        Path publicKey = createSamplePublicKey(inDir, name);
        return Map.entry(publicKey, privateKey);
    }

    static Path createSamplePublicKey(Path inDir) {
        return createSamplePublicKey(inDir, UUID.randomUUID().toString());
    }

    static Path createSamplePublicKey(Path inDir, String name) {
        Path publicKeyPath = inDir.resolve(name.concat(".pub"));
        try {
            Files.createFile(publicKeyPath);
            Files.write(publicKeyPath, List.of("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="));
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
        return publicKeyPath;
    }

    static Path createSamplePrivateKey(Path inDir) {
        return createSamplePrivateKey(inDir, UUID.randomUUID().toString());
    }

    static Path createSamplePrivateKey(Path inDir, String name) {
        String someEncodedBytes = Base64.getEncoder().encodeToString("SOMEDATA".getBytes());
        JsonObject privateKeyJson = Json.createObjectBuilder()
            .add("data",
                Json.createObjectBuilder()
                    .add("bytes", someEncodedBytes)
            )
            .add("type", "unlocked")
            .build();

        Path path = inDir.resolve(name.concat(".key"));
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            try (JsonWriter jsonWriter = Json.createWriter(outputStream)) {
                jsonWriter.writeObject(privateKeyJson);
            }
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
        return path;
    }

    @Test
    public void defaultConstructor() {

        KeyPasswordResolver keyPasswordResolver = mock(KeyPasswordResolver.class);
        try(var keyPasswordResolverMockedStatic = mockStatic(KeyPasswordResolver.class)) {
            keyPasswordResolverMockedStatic.when(KeyPasswordResolver::create).thenReturn(keyPasswordResolver);

            PicoCliDelegate instance = new PicoCliDelegate();
            assertThat(instance).isNotNull();

            keyPasswordResolverMockedStatic.verify(KeyPasswordResolver::create);
            keyPasswordResolverMockedStatic.verifyNoMoreInteractions();

        }
        verifyNoInteractions(keyPasswordResolver);
    }
}


