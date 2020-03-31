package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.ConfigFileUpdaterWriter;
import com.quorum.tessera.config.util.PasswordFileUpdaterWriter;
import com.quorum.tessera.key.generation.KeyGenerator;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import com.quorum.tessera.key.generation.KeyVaultOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeyGenCommandTest {

    private KeyGenCommand command;

    private KeyGeneratorFactory keyGeneratorFactory;

    private ConfigFileUpdaterWriter configFileUpdaterWriter;

    private PasswordFileUpdaterWriter passwordFileUpdaterWriter;

    private final CliResult wantResult = new CliResult(0, true, null);

    @Captor
    private ArgumentCaptor<ArrayList<String>> argCaptor;

    @Before
    public void onSetup() {
        MockitoAnnotations.initMocks(this);
        keyGeneratorFactory = mock(KeyGeneratorFactory.class);
        configFileUpdaterWriter = mock(ConfigFileUpdaterWriter.class);
        passwordFileUpdaterWriter = mock(PasswordFileUpdaterWriter.class);
        command = new KeyGenCommand(keyGeneratorFactory, configFileUpdaterWriter, passwordFileUpdaterWriter);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(keyGeneratorFactory, configFileUpdaterWriter, passwordFileUpdaterWriter);
    }

    @Test
    public void usesDefaultEncryptorConfigIfNoneInConfig() throws Exception {
        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(encryptorOptions).parseEncryptorConfig();
        verify(keyGenerator).generate(anyString(), any(), any());
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void usesDefaultEncryptorIfNoneInConfigOrCLI() throws Exception {
        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        ArgumentCaptor<EncryptorConfig> arg = ArgumentCaptor.forClass(EncryptorConfig.class);
        verify(keyGeneratorFactory).create(eq(null), arg.capture());
        EncryptorConfig gotEncryptorConfig = arg.getValue();
        assertThat(gotEncryptorConfig).isEqualToComparingFieldByField(EncryptorConfig.getDefault());

        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(anyString(), any(), any());
    }

    @Test
    public void doNotUseEncryptorOptionsIfConfigHasEncryptorConfig() throws Exception {
        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);
        final Config config = new Config();
        config.setEncryptor(encryptorConfig);
        final KeyGenFileUpdateOptions fileUpdateOptions = new KeyGenFileUpdateOptions();

        command.encryptorOptions = encryptorOptions;
        command.fileUpdateOptions = fileUpdateOptions;
        command.fileUpdateOptions.config = config;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(keyGenerator).generate(anyString(), any(), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);

        verify(configFileUpdaterWriter).updateAndWriteToCLI(any(), any(), any());
    }

    @Test
    public void noKeyEncryptionConfigUsesDefault() throws Exception {
        final ArgonOptions defaultArgonOptions = null;

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(anyString(), eq(defaultArgonOptions), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void providedKeyEncryptionConfigIsUsed() throws Exception {
        final ArgonOptions argonOptions = new ArgonOptions();

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;
        command.argonOptions = argonOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(anyString(), eq(argonOptions), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void noKeyOutputPathUsesDefault() throws Exception {
        final String defaultOutputPath = "";

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(eq(defaultOutputPath), any(), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void providedKeyOutputPathIsUsed() throws Exception {
        final String outputPath = "mynewkey";

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;
        command.keyOut = Arrays.asList(outputPath);

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(eq(outputPath), any(), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void multipleKeyOutputPathsGeneratesMultipleKeys() throws Exception {
        final String outputPath = "mynewkey";
        final String otherOutputPath = "myothernewkey";

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;
        command.keyOut = Arrays.asList(outputPath, otherOutputPath);

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(eq(outputPath), any(), any());
        verify(keyGenerator).generate(eq(otherOutputPath), any(), any());

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void noKeyVaultOptionsUsesDefault() throws Exception {
        final KeyVaultOptions defaultKeyVaultOptions = null;

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        command.encryptorOptions = encryptorOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(null, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(anyString(), any(), eq(defaultKeyVaultOptions));

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void providedKeyVaultOptionsAreUsed() throws Exception {
        final String keyVaultOptionValue = "somevalue";
        final KeyVaultOptions keyVaultOptions = new KeyVaultOptions(keyVaultOptionValue);

        final EncryptorConfig encryptorConfig = new EncryptorConfig();
        final Map<String, String> properties = new HashMap<>();
        encryptorConfig.setType(EncryptorType.NACL);
        encryptorConfig.setProperties(properties);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(encryptorConfig);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.HASHICORP);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn("someurl");
        when(keyVaultConfigOptions.getHashicorpSecretEnginePath()).thenReturn(keyVaultOptionValue);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;
        command.keyOut = Collections.singletonList("keyout");

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        KeyVaultConfig keyVaultConfig = new HashicorpKeyVaultConfig("someurl", null, null, null);
        verify(keyGeneratorFactory).create(keyVaultConfig, encryptorConfig);
        assertThat(result).isEqualToComparingFieldByField(wantResult);
        verify(keyGenerator).generate(anyString(), any(), refEq(keyVaultOptions));

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void validAzureKeyVaultConfig() throws Exception {
        final KeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("someurl");

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.AZURE);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn("someurl");

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(refEq(keyVaultConfig), any(EncryptorConfig.class));
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(keyGenerator).generate(anyString(), any(), any());
        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void invalidAzureKeyVaultConfigThrowsException() {
        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.AZURE);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(1);

        ConstraintViolation violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("url");
        assertThat(violation.getMessage()).isEqualTo("may not be null");

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions);
    }

    @Test
    public void validHashicorpKeyVaultConfig() throws Exception {
        final String vaultUrl = "someurl";
        final String approlePath = "someapprole";
        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        final KeyVaultConfig keyVaultConfig = new HashicorpKeyVaultConfig(vaultUrl, approlePath, tempPath, tempPath);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.HASHICORP);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn(vaultUrl);
        when(keyVaultConfigOptions.getHashicorpApprolePath()).thenReturn(approlePath);
        when(keyVaultConfigOptions.getHashicorpTlsKeystore()).thenReturn(tempPath);
        when(keyVaultConfigOptions.getHashicorpTlsTruststore()).thenReturn(tempPath);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;
        command.keyOut = Collections.singletonList("out");

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(refEq(keyVaultConfig), any(EncryptorConfig.class));
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(keyGenerator).generate(anyString(), any(), any());
        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void hashicorpKeyVaultConfigNoOutputPathsThrowsException() throws Exception {
        final String vaultUrl = "someurl";
        final String approlePath = "someapprole";
        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.HASHICORP);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn(vaultUrl);
        when(keyVaultConfigOptions.getHashicorpApprolePath()).thenReturn(approlePath);
        when(keyVaultConfigOptions.getHashicorpTlsKeystore()).thenReturn(tempPath);
        when(keyVaultConfigOptions.getHashicorpTlsTruststore()).thenReturn(tempPath);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(CliException.class);
        assertThat(ex)
                .hasMessage("At least one -filename must be provided when saving generated keys in a Hashicorp Vault");

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions);
    }

    @Test
    public void invalidHashicorpKeyVaultConfigThrowsException() {
        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.HASHICORP);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;
        command.keyOut = Collections.singletonList("out");

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(1);

        ConstraintViolation violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("url");
        assertThat(violation.getMessage()).isEqualTo("may not be null");

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions);
    }

    @Test
    public void hashicorpTlsPathsDontExistThrowsException() {
        final String vaultUrl = "someurl";
        final String approlePath = "someapprole";
        final Path nonExistentPath = Paths.get(UUID.randomUUID().toString());

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.HASHICORP);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn(vaultUrl);
        when(keyVaultConfigOptions.getHashicorpApprolePath()).thenReturn(approlePath);
        when(keyVaultConfigOptions.getHashicorpTlsKeystore()).thenReturn(nonExistentPath);
        when(keyVaultConfigOptions.getHashicorpTlsTruststore()).thenReturn(nonExistentPath);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;
        command.keyOut = Collections.singletonList("out");

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(2);

        Iterator<ConstraintViolation<?>> iterator = violations.iterator();

        assertThat(iterator.next().getMessage()).isEqualTo("File does not exist");
        assertThat(iterator.next().getMessage()).isEqualTo("File does not exist");

        // verify the correct config is used
        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void validAWSKeyVaultConfig() throws Exception {
        String endpointUrl = "https://someurl.com";

        final DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
        keyVaultConfig.setKeyVaultType(KeyVaultType.AWS);
        keyVaultConfig.setProperty("endpoint", endpointUrl);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.AWS);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn(endpointUrl);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(refEq(keyVaultConfig), any(EncryptorConfig.class));
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(keyGenerator).generate(anyString(), any(), any());
        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void validAWSKeyVaultConfigNoVaultUrl() throws Exception {
        final DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
        keyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.AWS);

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        CliResult result = command.call();

        // verify the correct config is used
        verify(keyGeneratorFactory).create(refEq(keyVaultConfig), any(EncryptorConfig.class));
        assertThat(result).isEqualToComparingFieldByField(wantResult);

        verify(keyGenerator).generate(anyString(), any(), any());
        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions, keyGenerator);
    }

    @Test
    public void invalidAWSKeyVaultConfigThrowsException() {
        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultType()).thenReturn(KeyVaultType.AWS);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn("not a valid url");

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(1);

        ConstraintViolation violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("must be a valid AWS service endpoint URL with scheme");

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions);
    }

    @Test
    public void vaultUrlButNoVaultTypeThrowsException() {
        final EncryptorOptions encryptorOptions = mock(EncryptorOptions.class);
        when(encryptorOptions.parseEncryptorConfig()).thenReturn(null);

        final KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
        when(keyVaultConfigOptions.getVaultUrl()).thenReturn("someurl");

        command.encryptorOptions = encryptorOptions;
        command.keyVaultConfigOptions = keyVaultConfigOptions;

        final KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);

        Throwable ex = catchThrowable(() -> command.call());

        assertThat(ex).isInstanceOf(CliException.class);
        assertThat(ex.getMessage()).isEqualTo("Key vault type either not provided or not recognised");

        verify(encryptorOptions).parseEncryptorConfig();
        verifyNoMoreInteractions(encryptorOptions);
    }

    @Test
    public void configAndConfigOutOptionsThenWriteUpdatedConfigFile() throws Exception {
        command.fileUpdateOptions = new KeyGenFileUpdateOptions();
        Config config = new Config();
        Path configOut = mock(Path.class);
        command.fileUpdateOptions.config = config;
        command.fileUpdateOptions.configOut = configOut;

        KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);
        when(keyGenerator.generate(any(), any(), any())).thenReturn(mock(ConfigKeyPair.class));

        command.call();

        verify(configFileUpdaterWriter).updateAndWrite(any(), any(), eq(config), eq(configOut));
        verify(keyGeneratorFactory).create(any(), any());
    }

    @Test
    public void configOutAndPwdOutOptionsThenWriteUpdatedConfigAndPasswordFiles() throws Exception {
        command.fileUpdateOptions = new KeyGenFileUpdateOptions();
        Config config = new Config();
        Path configOut = mock(Path.class);
        Path pwdOut = mock(Path.class);
        command.fileUpdateOptions.config = config;
        command.fileUpdateOptions.configOut = configOut;
        command.fileUpdateOptions.pwdOut = pwdOut;

        KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);
        ConfigKeyPair keyPair = mock(ConfigKeyPair.class);
        when(keyPair.getPassword()).thenReturn("pwd");

        when(keyGenerator.generate(any(), any(), any())).thenReturn(keyPair);

        command.call();

        verify(passwordFileUpdaterWriter).updateAndWrite(argCaptor.capture(), eq(config), eq(pwdOut));
        assertThat(argCaptor.getValue()).containsExactly("pwd");
        verify(configFileUpdaterWriter).updateAndWrite(any(), any(), eq(config), eq(configOut));
        verify(keyGeneratorFactory).create(any(), any());
    }

    @Test
    public void useKeyVaultConfigFromFileOverCliOptions() throws Exception {
        command.fileUpdateOptions = new KeyGenFileUpdateOptions();
        command.keyVaultConfigOptions = new KeyVaultConfigOptions();

        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);
        when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("should be used"));

        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        Path configOut = mock(Path.class);
        command.fileUpdateOptions.config = config;
        command.fileUpdateOptions.configOut = configOut;
        command.keyVaultConfigOptions.vaultType = KeyVaultType.AZURE;
        command.keyVaultConfigOptions.vaultUrl = "shouldnt be used";

        KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);
        when(keyGenerator.generate(any(), any(), any())).thenReturn(mock(ConfigKeyPair.class));

        command.call();

        ArgumentCaptor<KeyVaultConfig> captor = ArgumentCaptor.forClass(KeyVaultConfig.class);

        verify(configFileUpdaterWriter).updateAndWrite(any(), captor.capture(), eq(config), eq(configOut));

        KeyVaultConfig arg = captor.getValue();
        assertThat(arg).isNotNull();
        assertThat(arg.getKeyVaultType()).isEqualTo(KeyVaultType.AZURE);
        assertThat(arg.getProperty("url")).isNotEmpty();
        assertThat(arg.getProperty("url")).hasValue("should be used");

        verify(keyGeneratorFactory).create(any(), any());
    }

    @Test
    public void useKeyVaultConfigFromCliOptionsIfConfigFileValueIsForDifferentType() throws Exception {
        command.fileUpdateOptions = new KeyGenFileUpdateOptions();
        command.keyVaultConfigOptions = new KeyVaultConfigOptions();

        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);
        when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("azure in the existing file"));

        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        Path configOut = mock(Path.class);
        command.fileUpdateOptions.config = config;
        command.fileUpdateOptions.configOut = configOut;
        command.keyVaultConfigOptions.vaultType = KeyVaultType.AWS;
        command.keyVaultConfigOptions.vaultUrl = "http://awsforthenewkey";

        KeyGenerator keyGenerator = mock(KeyGenerator.class);
        when(keyGeneratorFactory.create(any(), any())).thenReturn(keyGenerator);
        when(keyGenerator.generate(any(), any(), any())).thenReturn(mock(ConfigKeyPair.class));

        command.call();

        ArgumentCaptor<KeyVaultConfig> captor = ArgumentCaptor.forClass(KeyVaultConfig.class);

        verify(configFileUpdaterWriter).updateAndWrite(any(), captor.capture(), eq(config), eq(configOut));

        KeyVaultConfig arg = captor.getValue();
        assertThat(arg).isNotNull();
        assertThat(arg.getKeyVaultType()).isEqualTo(KeyVaultType.AWS);
        assertThat(arg.getProperty("endpoint")).isNotEmpty();
        assertThat(arg.getProperty("endpoint")).hasValue("http://awsforthenewkey");

        verify(keyGeneratorFactory).create(any(), any());
    }
}
