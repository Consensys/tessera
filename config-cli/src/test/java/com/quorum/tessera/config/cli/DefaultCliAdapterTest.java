package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.keys.KeyGenerator;
import com.quorum.tessera.config.keys.KeyGeneratorFactory;
import com.quorum.tessera.config.keys.MockKeyGeneratorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import com.quorum.tessera.test.util.ElUtil;
import static com.quorum.tessera.test.util.ElUtil.*;
import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.stream.Collectors;


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
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute(
                "-configfile",
                configFile.toString());
        
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
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/missing-config.json"));
        
        try {
            cliDelegate.execute(
                    "-configfile",
                    configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(2);
            
        }
        
    }
    
    @Test
    public void keygen() throws Exception {
        
        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        
        KeyData keyData = mock(KeyData.class);
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyData.getConfig()).thenReturn(keyDataConfig);
        
        when(keyGenerator.generate(anyString(), eq(null))).thenReturn(keyData);
        
        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
        
        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());
        
        Path configFilePath = ElUtil.createTempFileFromTemplate(getClass().getResource("/keygen-sample.json"), params);
        
        CliResult result = cliDelegate.execute(
                "-keygen",
                "-filename",
                UUID.randomUUID().toString(),
                "-configfile",
                configFilePath.toString());
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.isHelpOn()).isFalse();
        
        verify(keyGenerator).generate(anyString(), eq(null));
        verifyNoMoreInteractions(keyGenerator);
        
        Files.deleteIfExists(unixSocketPath);
        
    }
    
    @Test
    public void keygenWithNoName() throws Exception {
        
        final InputStream tempSystemIn = new ByteArrayInputStream(System.lineSeparator().getBytes());
        
        final InputStream oldSystemIn = System.in;
        System.setIn(tempSystemIn);
        
        final CliResult result = cliDelegate.execute(
                "-keygen"
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
        
        try {
            cliDelegate.execute(
                    "-filename"
            );
            failBecauseExceptionWasNotThrown(CliException.class);
        } catch (CliException ex) {
            assertThat(ex).hasMessage("Missing argument for option: filename");
        }
    }

    @Test
    public void outputWithoutKeygenOrConfig() throws Exception {

        try {
            cliDelegate.execute(
                "-output","bogus"
            );
            failBecauseExceptionWasNotThrown(CliException.class);
        } catch (CliException ex) {
            assertThat(ex).hasMessage("One or both: -configfile <PATH> or -keygen options are required.");
        }
    }
    
    @Test
    public void output() throws Exception {
        
        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        
        KeyData keyData = mock(KeyData.class);
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyData.getConfig()).thenReturn(keyDataConfig);
        
        when(keyGenerator.generate(anyString(), eq(null))).thenReturn(keyData);
        
        Path generatedKey = Paths.get("/tmp/" + UUID.randomUUID().toString());
        
        Files.deleteIfExists(generatedKey);
        assertThat(Files.exists(generatedKey)).isFalse();
        
        Path keyConfigPath = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());
        Path tempKeyFile = Files.createTempFile(UUID.randomUUID().toString(), "");
        
        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/keygen-sample.json"));
        
        CliResult result = cliDelegate.execute(
                "-keygen",
                keyConfigPath.toString(),
                "-filename",
                tempKeyFile.toAbsolutePath().toString(),
                "-output",
                generatedKey.toFile().getPath(),
                "-configfile",
                configFile.toString()
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
                    configFile.toString()
            );
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(FileAlreadyExistsException.class);
        }
        
        Files.deleteIfExists(generatedKey);
        assertThat(Files.exists(generatedKey)).isFalse();
        
    }
    
    @Test
    public void pidFile() throws Exception {
        
        Path pidFile = Paths.get(getClass().getResource("/pid").getFile());
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        
        CliResult result = cliDelegate.execute(
                "-pidfile",
                pidFile.toFile().getPath(),
                "-configfile",
                configFile.toString()
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
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        
        CliResult result = cliDelegate.execute(
                "-pidfile",
                anotherPidFile.toFile().getPath(),
                "-configfile",
                configFile.toString()
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
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        
        CliResult result = cliDelegate.execute(
                "-configfile",
                configFile.toString(),
                "-jdbc.username",
                "somename"
        );
        
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getJdbcConfig().getUsername()).isEqualTo("somename");
        assertThat(result.getConfig().get().getJdbcConfig().getPassword()).isEqualTo("tiger");
        
    }
    
    @Test
    public void providingArgonOptionsGetSentCorrectly() throws Exception {
        final String options = "{\"variant\": \"id\",\"memory\": 100,\"iterations\": 7,\"parallelism\": 22}";
        final Path argonOptions = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(argonOptions, options.getBytes());
        
        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");
        
        final CliResult result = cliDelegate.execute(
                "-keygen",
                "-keygenconfig", argonOptions.toString(),
                "-filename", keyLocation.toString()
        );
        
        assertThat(result).isNotNull();
        assertThat(result.isKeyGenOn()).isTrue();
        
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create();
        
        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture());
        
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue().getAlgorithm()).isEqualTo("id");
        assertThat(captor.getValue().getIterations()).isEqualTo(7);
        assertThat(captor.getValue().getMemory()).isEqualTo(100);
        assertThat(captor.getValue().getParallelism()).isEqualTo(22);
    }
    
    @Test
    public void notProvidingArgonOptionsGivesNull() throws Exception {
        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");
        
        final CliResult result = cliDelegate.execute(
                "-keygen",
                "-filename", keyLocation.toString()
        );
        
        assertThat(result).isNotNull();
        assertThat(result.isKeyGenOn()).isTrue();
        
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create();
        
        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture());
        
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isNull();
    }
    
    @Test
    public void withInvalidPath() throws Exception {
        //unixSocketPath
        Map<String, Object> params = new HashMap<>();
        params.put("publicKeyPath", "BOGUS.bogus");
        params.put("privateKeyPath", "BOGUS.bogus");
        
        Path configFile = ElUtil.createTempFileFromTemplate(
                getClass().getResource("/sample-config-invalidpath.json"), params);
        
        try {
            cliDelegate.execute(
                    "-configfile",
                    configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(2);
          
            ex.getConstraintViolations().forEach(System.out::println);
            
            List<String> invalidPaths = ex.getConstraintViolations().stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Objects::toString)
                    .collect(Collectors.toList());
            
            assertThat(invalidPaths).containsExactlyInAnyOrder(
                    "keys.keyData","keys.keyData"
            );
            

            
        }
        
    }
    
}
