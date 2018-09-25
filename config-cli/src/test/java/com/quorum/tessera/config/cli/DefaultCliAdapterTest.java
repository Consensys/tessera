package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyGenerator;
import com.quorum.tessera.config.keys.MockKeyGeneratorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.util.ElUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DefaultCliAdapterTest {
    
    private CliAdapter cliDelegate;
    
    @Before
    public void setUp() {
        MockKeyGeneratorFactory.reset();
        this.cliDelegate = CliAdapter.create();
    }
    
    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("/tmp/anotherPrivateKey.key").toAbsolutePath());
        Files.deleteIfExists(Paths.get("/tmp/anotherPublicKey.key").toAbsolutePath());
    }
    
    @Test
    public void help() throws Exception {
        
        final CliResult result = cliDelegate.execute("help");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isTrue();
        assertThat(result.isKeyGenOn()).isFalse();
        
    }
    
    @Test
    public void noArgsPrintsHelp() throws Exception {
        
        final CliResult result = cliDelegate.execute();

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isTrue();
        assertThat(result.isKeyGenOn()).isFalse();
        
    }
    
    @Test
    public void withValidConfig() throws Exception {
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        CliResult result = cliDelegate.execute("-configfile", configFile.toString());
        
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.isHelpOn()).isFalse();
    }
    
    @Test(expected = FileNotFoundException.class)
    public void callApiVersionWithConfigFileDoesNotExist() throws Exception {
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
            cliDelegate.execute("-configfile", configFile.toString());
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).hasSize(2);
            
        }
        
    }
    
    @Test
    public void keygen() throws Exception {
        
        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        FilesystemKeyPair keypair = new FilesystemKeyPair(Paths.get(""), Paths.get(""));
        when(keyGenerator.generate(anyString(), eq(null))).thenReturn(keypair);
        
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
        
    }
    
    @Test
    public void keygenThenExit() throws Exception {

        final CliResult result = cliDelegate.execute("-keygen");
        
        assertThat(result).isNotNull();
        assertThat(result.isKeyGenOn()).isTrue();
        
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

        final Throwable throwable = catchThrowable(() ->  cliDelegate.execute("-output","bogus"));
        assertThat(throwable)
            .isInstanceOf(CliException.class)
            .hasMessage("One or more: -configfile or -keygen or -updatepassword options are required.");

    }
    
    @Test
    public void output() throws Exception {
        
        KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        FilesystemKeyPair keypair = new FilesystemKeyPair(Paths.get(""), Paths.get(""));
        when(keyGenerator.generate(anyString(), eq(null))).thenReturn(keypair);
        
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
            assertThat(ex.getConstraintViolations())
                .hasSize(2)
                .extracting("messageTemplate")
                .containsExactly("{ValidPath.message}", "{ValidPath.message}");
        }
        
    }

    @Test
    public void overrideAlwaysSendTo() throws Exception {

        String alwaysSendToKey = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";
        
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        
        CliResult result = cliDelegate.execute(
                "-configfile",
                configFile.toString(),
                "-alwaysSendTo",
                alwaysSendToKey
        );
        
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getAlwaysSendTo()).hasSize(2);
        assertThat(result.getConfig().get().getAlwaysSendTo()).containsExactly("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",alwaysSendToKey);
        
    }
    
    @Test
    public void overridePeers() throws Exception{
    
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));
        
        CliResult result = cliDelegate.execute(
                "-configfile",
                configFile.toString(),
                "-peer.url",
                "anotherpeer",
                "-peer.url",
                "yetanotherpeer"
        );

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getConfig().get().getPeers()).hasSize(4);
        assertThat(result.getConfig().get().getPeers().stream()
                .map(Peer::getUrl))
                .containsExactlyInAnyOrder("anotherpeer","yetanotherpeer","http://bogus1.com","http://bogus2.com");
        
    }

    @Test
    public void updatingPasswordsDoesntProcessOtherOptions() throws Exception {
        MockKeyGeneratorFactory.reset();

        final InputStream oldIn = System.in;
        final InputStream inputStream = new ByteArrayInputStream(
            (System.lineSeparator() + System.lineSeparator()).getBytes()
        );
        System.setIn(inputStream);

        final KeyDataConfig startingKey = JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class
        );

        final Path key = Files.createTempFile("key", ".key");
        Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

        final CliResult result = cliDelegate.execute(
            "-updatepassword",
            "--keys.keyData.privateKeyPath", key.toString(),
            "--keys.passwords", "q",
            "-keygen"
        );

        assertThat(result).isNotNull();

        verifyZeroInteractions(MockKeyGeneratorFactory.getMockKeyGenerator());
        System.setIn(oldIn);
    }
    
}
