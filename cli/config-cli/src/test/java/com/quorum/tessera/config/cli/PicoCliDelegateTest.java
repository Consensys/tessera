package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.cli.keys.MockKeyGeneratorFactory;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.test.util.ElUtil;
import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolationException;
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

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PicoCliDelegateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegateTest.class);

    private PicoCliDelegate cliDelegate;

    @Before
    public void setUp() {
        cliDelegate = new PicoCliDelegate();
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

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();
    }

    @Test
    public void withValidConfigAndPidfile() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

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
    }

    @Test
    public void withValidConfigAndPidfileAlreadyExists() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
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
    }

    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        cliDelegate.execute("-configfile");
    }

    @Test
    public void withConstraintViolations() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/missing-config.json"));

        try {
            cliDelegate.execute("-configfile", configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).isNotEmpty();
        }
    }

    @Test
    public void keygen() throws Exception {
        MockKeyGeneratorFactory.reset();

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        FilesystemKeyPair keypair = mock(FilesystemKeyPair.class);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        CliResult result = cliDelegate.execute("-keygen", "-filename", UUID.randomUUID().toString());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isSuppressStartup()).isTrue();

        verify(keyGenerator).generate(anyString(), eq(null), eq(null));
        verifyNoMoreInteractions(keyGenerator);
    }

    @Test
    public void keygenThenExit() throws Exception {

        final CliResult result = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

        assertThat(result).isNotNull();
        assertThat(result.isSuppressStartup()).isTrue();
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
        MockKeyGeneratorFactory.reset();

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

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

        Path configFile = createAndPopulatePaths(getClass().getResource("/keygen-sample.json"));
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
    public void outputConfigAndPasswordFiles() throws Exception {
        MockKeyGeneratorFactory.reset();

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

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

        Path configFile = createAndPopulatePaths(getClass().getResource("/keygen-sample.json"));
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
        MockKeyGeneratorFactory.reset();

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

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

        Path configFile = createAndPopulatePaths(getClass().getResource("/keygen-sample.json"));
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

    @Test
    public void keygenFileUpdateOptionsRequireConfigfile() {
        Throwable ex = catchThrowable(() -> cliDelegate.execute("-keygen", "-output", "somepath"));

        assertThat(ex).isNotNull();
        assertThat(ex).isExactlyInstanceOf(CliException.class);
        assertThat(ex.getMessage()).contains("Missing required argument(s): --configfile=<config>");
    }

    @Test
    public void dynOption() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-o", "jdbc.username=somename");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getJdbcConfig().getUsername()).isEqualTo("somename");
        assertThat(result.getConfig().get().getJdbcConfig().getPassword()).isEqualTo("tiger");
    }

    @Ignore
    @Test
    public void withInvalidPath() throws Exception {
        // unixSocketPath
        Map<String, Object> params = new HashMap<>();
        params.put("publicKeyPath", "BOGUS.bogus");
        params.put("privateKeyPath", "BOGUS.bogus");

        Path configFile =
                ElUtil.createTempFileFromTemplate(getClass().getResource("/sample-config-invalidpath.json"), params);

        try {
            cliDelegate.execute("-configfile", configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations())
                    .hasSize(2)
                    .extracting("messageTemplate")
                    .containsExactly("File does not exist", "File does not exist");
        }
    }

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

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
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

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result =
                cliDelegate.execute(
                        "-configfile", configFile.toString(),
                        "-o", "peer[2].url=anotherpeer",
                        "--override", "peer[3].url=yetanotherpeer");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getPeers()).hasSize(4);
        assertThat(result.getConfig().get().getPeers().stream().map(Peer::getUrl))
                .containsExactlyInAnyOrder("anotherpeer", "yetanotherpeer", "http://bogus1.com", "http://bogus2.com");
    }

    @Test
    public void updatingPasswordsDoesntProcessOtherOptions() throws Exception {
        MockKeyGeneratorFactory.reset();

        final InputStream oldIn = System.in;
        final InputStream inputStream =
                new ByteArrayInputStream((System.lineSeparator() + System.lineSeparator()).getBytes());
        System.setIn(inputStream);

        final KeyDataConfig startingKey =
                JaxbUtil.unmarshal(getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

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

        Mockito.verifyZeroInteractions(MockKeyGeneratorFactory.getMockKeyGenerator());
        System.setIn(oldIn);
    }

    @Test
    public void suppressStartupForKeygenOption() throws Exception {
        final CliResult cliResult = cliDelegate.execute("-keygen", "--encryptor.type", "NACL");

        assertThat(cliResult.isSuppressStartup()).isTrue();
    }

    @Test
    public void suppressStartupForKeygenOptionWithFileOutputOptions() throws Exception {
        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

        Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
        Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        final Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        final Path configOutputPath = configFile.resolveSibling(UUID.randomUUID().toString() + ".json");

        final CliResult cliResult =
                cliDelegate.execute(
                        "-keygen", "-configfile", configFile.toString(), "-output", configOutputPath.toString());

        assertThat(cliResult.isSuppressStartup()).isTrue();
    }

    @Test
    public void subcommandExceptionIsThrown() {
        Throwable ex = catchThrowable(() -> cliDelegate.execute("-keygen", "-keygenvaulturl", "urlButNoVaultType"));

        assertThat(ex).isNotNull();
        assertThat(ex).isInstanceOf(CliException.class);
    }

    @Test
    public void withValidConfigAndJdbcOveride() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-jdbc.autoCreateTables", "true");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        Config config = result.getConfig().get();
        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().isAutoCreateTables()).isTrue();
    }

    @Test
    public void withValidConfigAndUnmatchableDynamicOption() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-bogus");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();
    }

    @Test
    public void withValidConfigAndUnmatchableDynamicOptionWithValue() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-bogus", "bogus value");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();
    }

    @Test
    public void withValidConfigAndJdbcOverides() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result =
                cliDelegate.execute(
                        "-configfile", configFile.toString(), "-jdbc.autoCreateTables", "true", "-jdbc.url", "someurl");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.isSuppressStartup()).isFalse();

        Config config = result.getConfig().get();
        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().isAutoCreateTables()).isTrue();
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("someurl");
    }
}
