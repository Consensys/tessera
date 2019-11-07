package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.cli.keys.MockKeyGeneratorFactory;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import java.nio.file.NoSuchFileException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DefaultCliAdapterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCliAdapterTest.class);

    private DefaultCliAdapter cliDelegate;

    @Before
    public void setUp() {
        MockKeyGeneratorFactory.reset();
        this.cliDelegate = new DefaultCliAdapter();
    }

    @Test
    public void getType() {
        assertThat(cliDelegate.getType()).isEqualTo(CliType.CONFIG);
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
    public void helpViaCall() throws Exception {
        cliDelegate.setAllParameters(new String[] {"help"});
        final CliResult result = cliDelegate.call();

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
    public void withValidConfig() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isSuppressStartup()).isFalse();
    }

    @Test
    public void callApiVersionWithConfigFileDoesNotExist() throws Exception {
        try {
            cliDelegate.execute("-configfile", "bogus.json");
            fail("Shoudl have thrown an exception");
        } catch (FileNotFoundException | NoSuchFileException ex) {
            assertThat(ex).hasMessageContaining("bogus.json");
        }
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
    public void keygenWithConfig() throws Exception {

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

        Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
        Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyDataConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        Path configFilePath = ElUtil.createTempFileFromTemplate(getClass().getResource("/keygen-sample.json"), params);

        CliResult result =
                cliDelegate.execute(
                        "-keygen", "-filename", UUID.randomUUID().toString(), "-configfile", configFilePath.toString());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isSuppressStartup()).isFalse();

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
    public void fileNameWithoutKeygenArgThenExit() throws Exception {

        try {
            cliDelegate.execute("-filename");
            failBecauseExceptionWasNotThrown(CliException.class);
        } catch (CliException ex) {
            assertThat(ex).hasMessage("Missing argument for option: filename");
        }
    }

    @Test
    public void outputWithoutKeygenOrConfig() {

        final Throwable throwable = catchThrowable(() -> cliDelegate.execute("-output", "bogus"));
        assertThat(throwable)
                .isInstanceOf(CliException.class)
                .hasMessage("One or more: -configfile or -keygen or -updatepassword options are required.");
    }

    @Test
    public void output() throws Exception {

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

        Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
        Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

        InlineKeypair inlineKeypair = mock(InlineKeypair.class);

        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyDataConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);
        when(inlineKeypair.getPrivateKeyConfig()).thenReturn(keyDataConfig);

        KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, keyEncryptor);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        Path generatedKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        Files.deleteIfExists(generatedKey);
        assertThat(Files.exists(generatedKey)).isFalse();

        Path keyConfigPath = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
        Path tempKeyFile = Files.createTempFile(UUID.randomUUID().toString(), "");

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        Path configFile = createAndPopulatePaths(getClass().getResource("/keygen-sample.json"));

        CliResult result =
                cliDelegate.execute(
                        "-keygen", keyConfigPath.toString(),
                        "-filename", tempKeyFile.toAbsolutePath().toString(),
                        "-output", generatedKey.toFile().getPath(),
                        "-configfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(Files.exists(generatedKey)).isTrue();

        try {
            cliDelegate.execute(
                    "-keygen", keyConfigPath.toString(),
                    "-filename", UUID.randomUUID().toString(),
                    "-output", generatedKey.toFile().getPath(),
                    "-configfile", configFile.toString());
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(FileAlreadyExistsException.class);
        }
    }

    @Test
    public void dynOption() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        CliResult result = cliDelegate.execute("-configfile", configFile.toString(), "-jdbc.username", "somename");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getJdbcConfig().getUsername()).isEqualTo("somename");
        assertThat(result.getConfig().get().getJdbcConfig().getPassword()).isEqualTo("tiger");
    }

    @Ignore
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
                    .hasSize(1)
                    .extracting("messageTemplate")
                    .containsExactly("{UnsupportedKeyPair.message}");
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
                            "--unixSocketFile",
                            unixSocketFile.toString(),
                            "--encryptor.type",
                            "NACL");

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
            result = cliDelegate.execute("-configfile", configFile.toString(), "-alwaysSendTo", alwaysSendToKey);
        } catch (Exception ex) {
            ex.printStackTrace();
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
                        "-configfile",
                        configFile.toString(),
                        "-peer.url",
                        "anotherpeer",
                        "-peer.url",
                        "yetanotherpeer");

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
                        "testpassword",
                        "-keygen",
                        "--encryptor.type",
                        "NACL");

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
    public void allowStartupForKeygenAndConfigfileOptions() throws Exception {
        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        Path publicKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        Path privateKeyPath = Files.createTempFile(UUID.randomUUID().toString(), "");

        Files.write(privateKeyPath, Arrays.asList("SOMEDATA"));
        Files.write(publicKeyPath, Arrays.asList("SOMEDATA"));

        FilesystemKeyPair keypair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        final Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        final CliResult cliResult = cliDelegate.execute("-keygen", "-configfile", configFile.toString());

        assertThat(cliResult.isSuppressStartup()).isFalse();
    }

    @Test
    public void suppressStartupForKeygenAndVaultUrlAndConfigfileOptions() throws Exception {
        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        final FilesystemKeyPair keypair = new FilesystemKeyPair(Paths.get(""), Paths.get(""), null);
        when(keyGenerator.generate(anyString(), eq(null), eq(null))).thenReturn(keypair);

        final Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        final String vaultUrl = "https://test.vault.azure.net";

        final CliResult cliResult =
                cliDelegate.execute(
                        "-keygen",
                        "-keygenvaulttype",
                        "AZURE",
                        "-keygenvaulturl",
                        vaultUrl,
                        "-configfile",
                        configFile.toString());

        assertThat(cliResult.isSuppressStartup()).isTrue();
    }
}
