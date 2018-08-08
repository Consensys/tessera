package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.keys.KeyGenerator;
import com.quorum.tessera.config.keys.MockKeyGeneratorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DefaultCliAdapterTest {

    private CliAdapter cliDelegate;

    @Before
    public void setUp() {
        cliDelegate = CliAdapter.create();
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("/tmp/anotherPrivateKey.key").toAbsolutePath());
        Files.deleteIfExists(Paths.get("/tmp/anotherPublicKey.key").toAbsolutePath());
    }

    @Test
    public void help() throws Exception {

        CliResult result = cliDelegate.execute("help");
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isTrue();
        assertThat(result.isKeyGenOn()).isFalse();

    }

    @Test
    public void noArgsPrintsHelp() throws Exception {

        CliResult result = cliDelegate.execute();
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isTrue();
        assertThat(result.isKeyGenOn()).isFalse();

    }

    @Test
    public void withValidConfig() throws Exception {

        CliResult result = cliDelegate.execute(
                "-configfile",
                getClass().getResource("/sample-config.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isFalse();
    }

    @Test(expected = FileNotFoundException.class)
    public void callApiVersionWithConfigFileDoesnotExist() throws Exception {
        cliDelegate.execute("-configfile", "bogus.json");
    }

    @Test(expected = CliException.class)
    public void processArgsMissing() throws Exception {
        cliDelegate.execute("-configfile");
    }

    @Test
    public void withConstraintViolations() throws Exception {

        try {
            cliDelegate.execute(
                    "-configfile",
                    getClass().getResource("/missing-config.json").getFile());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(1);
        }

    }

    @Test
    public void keygen() throws Exception {

        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        KeyData keyData = mock(KeyData.class);
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyData.getConfig()).thenReturn(keyDataConfig);

        when(keyGenerator.generate(anyString())).thenReturn(keyData);

        Path keyConfigPath = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());

        CliResult result = cliDelegate.execute(
                "-keygen",
                "-filename",
                UUID.randomUUID().toString(),
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isHelpOn()).isFalse();

        verify(keyGenerator).generate(anyString());
        verifyNoMoreInteractions(keyGenerator);

    }

    @Test
    public void keygenWithNoName() throws Exception {

        final InputStream tempSystemIn = new ByteArrayInputStream(System.lineSeparator().getBytes());

        final InputStream oldSystemIn = System.in;
        System.setIn(tempSystemIn);

        final CliResult result = cliDelegate.execute(
            "-keygen",
            "-filename"
        );

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isHelpOn()).isFalse();

        System.setIn(oldSystemIn);

    }

    @Test
    public void keygenThenExit() throws Exception {

        Path keyConfigPath = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());

        CliResult result = cliDelegate.execute(
            "-keygen",
            keyConfigPath.toString()
        );

        assertThat(result).isNotNull();
        assertThat(result.isKeyGenOn()).isTrue();

    }


    @Test
    public void fileNameWithoutKeygenArgThenExit() throws Exception {

        CliResult result = cliDelegate.execute(
            "-filename"
        );

        assertThat(result).isNotNull();
        assertThat(result.isKeyGenOn()).isFalse();

    }

    @Test
    public void output() throws Exception {

        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        KeyData keyData = mock(KeyData.class);
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyData.getConfig()).thenReturn(keyDataConfig);

        when(keyGenerator.generate(anyString())).thenReturn(keyData);

        Path keyConfigPath = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
        Path generatedKey = Paths.get("/tmp/" + UUID.randomUUID().toString());
        Path tempKeyFile = Files.createTempFile(UUID.randomUUID().toString(), "");

        Files.deleteIfExists(generatedKey);
        assertThat(Files.exists(generatedKey)).isFalse();

        CliResult result = cliDelegate.execute(
            "-keygen",
            keyConfigPath.toString(),
            "-filename",
            tempKeyFile.toAbsolutePath().toString(),
            "-output",
            generatedKey.toFile().getPath(),
            "-configfile",
            getClass().getResource("/keygen-sample.json").getFile()
        );

        assertThat(result).isNotNull();
        assertThat(Files.exists(generatedKey)).isTrue();

        try {
            CliResult anotherResult = cliDelegate.execute(
                "-keygen",
                keyConfigPath.toString(),
                "-filename",
                UUID.randomUUID().toString(),
                "-output",
                generatedKey.toFile().getPath(),
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile()
            );
            failBecauseExceptionWasNotThrown(Exception.class);
        }
        catch (Exception ex) {
            assertThat(ex).isInstanceOf(FileAlreadyExistsException.class);
        }

        Files.deleteIfExists(generatedKey);
        assertThat(Files.exists(generatedKey)).isFalse();

    }

    @Test
    public void pidFile() throws Exception {

        Path pidFile = Paths.get(getClass().getResource("/pid").getFile());

        CliResult result = cliDelegate.execute(
                "-pidfile",
                pidFile.toFile().getPath(),
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile()
        );

        assertThat(result).isNotNull();

        try (InputStream in = Files.newInputStream(pidFile)) {
            assertThat(in.read()).isGreaterThan(1);
        }

    }

    @Test
    public void pidFileNotExisted() throws Exception {

        Path anotherPidFile = Paths.get("/tmp/anotherPidFile");

        assertThat(Files.notExists(anotherPidFile)).isTrue();

        CliResult result = cliDelegate.execute(
                "-pidfile",
                anotherPidFile.toFile().getPath(),
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile()
        );

        assertThat(result).isNotNull();
        assertThat(Files.exists(anotherPidFile)).isTrue();

        try (InputStream in = Files.newInputStream(anotherPidFile)) {
            assertThat(in.read()).isGreaterThan(1);
        }

        Files.deleteIfExists(anotherPidFile);

    }

    @Test
    public void dynOption() throws Exception {

        CliResult result = cliDelegate.execute(
                "-configfile",
                getClass().getResource("/keygen-sample.json").getFile(),
                "-jdbc.username",
                "somename"
        );

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getJdbcConfig().getUsername()).isEqualTo("somename");
        assertThat(result.getConfig().get().getJdbcConfig().getPassword()).isEqualTo("tiger");

    }
}
