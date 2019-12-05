package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.io.FilesDelegate;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.quorum.tessera.cli.parsers.ConfigurationParser.NEW_PASSWORD_FILE_PERMS;
import static com.quorum.tessera.cli.parsers.ConfigurationParser.passwordsMessage;
import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import static com.quorum.tessera.test.util.ElUtil.createTempFileFromTemplate;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class ConfigurationParserTest {

    private CommandLine commandLine;
    private FilesDelegate filesDelegate;

    @Before
    public void setUp() {
        commandLine = mock(CommandLine.class);
        filesDelegate = mock(FilesDelegate.class);
    }

    @Test
    public void noConfigfileOptionThenDoNothing() throws Exception {
        when(commandLine.hasOption("configfile")).thenReturn(false);
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST, filesDelegate);
        Config result = configParser.parse(commandLine);

        assertThat(result).isNull();

        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void configReadFromFile() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Config result = configParser.parse(commandLine);

        assertThat(result).isNotNull();
    }

    @Test
    public void configfileDoesNotExistThrowsException() {
        String path = "does/not/exist.config";

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(path);
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
        assertThat(throwable).hasMessage(path + " not found.");
    }

    @Test
    public void providingKeygenAndVaultOptionsThenConfigfileNotParsed() throws Exception {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Config result = configParser.parse(commandLine);

        assertThat(result).isNull();
    }

    @Test
    public void providingKeygenOptionThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");

        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        // FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void providingVaultOptionThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        // FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void providingNeitherKeygenOptionsThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        // FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void withNewKeysOutputsNewConfigToSystemAdapter() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        ConfigKeyPair newKey = new DirectKeyPair("pub", "priv");

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);
        Config result = configParser.parse(commandLine);

        in.close();
        assertThat(result).isNotNull();
        assertThat(result.getKeys().getKeyData()).contains(newKey);

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).newInputStream(configFile);
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void withNewKeysAndOutputOptionWritesNewConfigToFile() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        Path output = Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        ConfigKeyPair newKey = new DirectKeyPair("pub", "priv");

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(output, CREATE_NEW)).thenReturn(os);

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);

        Config result = configParser.parse(commandLine);

        in.close();

        assertThat(result).isNotNull();
        assertThat(result.getKeys().getKeyData()).contains(newKey);

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).newInputStream(configFile);
        verify(filesDelegate).newOutputStream(output, CREATE_NEW);
        verifyNoMoreInteractions(filesDelegate);

        byte[] bytesOut = os.toByteArray();
        assertThat(bytesOut).isNotEmpty();
    }

    @Test
    public void withNewKeysAndNullKeyConfig() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config-no-keyconfig.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        String tempDir = System.getProperty("java.io.tmpdir");

        Path output = Paths.get(tempDir, UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        ConfigKeyPair newKey = new DirectKeyPair("pub", "priv");

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey));

        Config result = configParser.parse(commandLine);

        assertThat(result).isNotNull();
        assertThat(result.getKeys().getKeyData()).contains(newKey);

        assertThat(output).exists();
        output.toFile().deleteOnExit();
    }

    @Test
    public void withNewPasswordProtectedKeysAndPasswordsListInConfigThrowsException() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        String tempDir = System.getProperty("java.io.tmpdir");

        Path output = Paths.get(tempDir, UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        when(newKey.getPassword()).thenReturn("A TEST PASSWORD");

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);

        Throwable ex = catchThrowable(() -> configParser.parse(commandLine));

        in.close();

        assertThat(ex).isExactlyInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains(passwordsMessage);

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).newInputStream(configFile);
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void withNewPasswordProtectedKeysAndNullKeyConfigThrowsException() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config-no-keyconfig.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        String tempDir = System.getProperty("java.io.tmpdir");

        Path output = Paths.get(tempDir, UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        when(newKey.getPassword()).thenReturn("A TEST PASSWORD");

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);

        Throwable ex = catchThrowable(() -> configParser.parse(commandLine));

        in.close();

        assertThat(ex).isExactlyInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains(passwordsMessage);

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).newInputStream(configFile);
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void withNewPasswordProtectedKeysAndExistingPasswordFileInConfigUpdatesConfigfileAndPasswordFile() throws Exception {

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
        Path passwordFilePath = Files.createTempFile(UUID.randomUUID().toString(), ".pwds");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());
        params.put("passwordFilePath", passwordFilePath.toString());

        Path configFile = createTempFileFromTemplate(getClass().getResource("/sample-config-password-file.json"), params);

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        String tempDir = System.getProperty("java.io.tmpdir");

        Path output = Paths.get(tempDir, UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.notExists(passwordFilePath)).thenReturn(false);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(output, CREATE_NEW)).thenReturn(os);

        ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        final String testPassword = "A TEST PASSWORD";
        when(newKey.getPassword()).thenReturn(testPassword);

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);

        Config result = configParser.parse(commandLine);

        in.close();

        assertThat(result).isNotNull();

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).notExists(passwordFilePath);
        verify(filesDelegate).newInputStream(configFile);
        verify(filesDelegate).newOutputStream(output, CREATE_NEW);
        verify(filesDelegate).write(passwordFilePath, Arrays.asList(testPassword), APPEND);
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void withNewPasswordProtectedKeysAndNonExistingPasswordFileInConfigUpdatesConfigfileAndCreatesPasswordFile() throws Exception {

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");
        Path passwordFilePath = Files.createTempFile(UUID.randomUUID().toString(), ".pwds");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());
        params.put("passwordFilePath", passwordFilePath.toString());

        Path configFile = createTempFileFromTemplate(getClass().getResource("/sample-config-password-file.json"), params);

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        String tempDir = System.getProperty("java.io.tmpdir");

        Path output = Paths.get(tempDir, UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        FilesDelegate fd = new FilesDelegate() {};
        InputStream in = fd.newInputStream(configFile);

        when(filesDelegate.exists(configFile)).thenReturn(true);
        when(filesDelegate.notExists(passwordFilePath)).thenReturn(true);
        when(filesDelegate.newInputStream(configFile)).thenReturn(in);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(output, CREATE_NEW)).thenReturn(os);

        ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        final String testPassword = "A TEST PASSWORD";
        when(newKey.getPassword()).thenReturn(testPassword);

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey), filesDelegate);

        Config result = configParser.parse(commandLine);

        in.close();

        assertThat(result).isNotNull();

        verify(filesDelegate).exists(configFile);
        verify(filesDelegate).notExists(passwordFilePath);
        verify(filesDelegate).createFile(passwordFilePath);
        verify(filesDelegate).setPosixFilePermissions(passwordFilePath, NEW_PASSWORD_FILE_PERMS);
        verify(filesDelegate).newInputStream(configFile);
        verify(filesDelegate).newOutputStream(output, CREATE_NEW);
        verify(filesDelegate).write(passwordFilePath, Arrays.asList(testPassword), APPEND);
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void doPasswordStuffWithEmptyPasswordsElement() throws Exception {

        final String password = "I LOVE SPARROWS!";
        final ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        when(newKey.getPassword()).thenReturn(password);

        final List<ConfigKeyPair> newKeys = Arrays.asList(newKey);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        final ConfigurationParser configParser = new ConfigurationParser(newKeys, filesDelegate);

        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getPasswords()).thenReturn(new ArrayList<>());
        when(config.getKeys()).thenReturn(keyConfiguration);

        Throwable ex = catchThrowable(() -> configParser.doPasswordStuff(config));

        assertThat(ex).isInstanceOf(ConfigException.class);
        verifyZeroInteractions(filesDelegate);
    }

    @Test
    public void doPasswordStuffWithPasswordFileDefined() throws Exception {

        final String password = "I LOVE SPARROWS!";

        final ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        when(newKey.getPassword()).thenReturn(password);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        final List<ConfigKeyPair> newKeys = Arrays.asList(newKey);

        final ConfigurationParser configParser = new ConfigurationParser(newKeys, filesDelegate);

        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getPasswords()).thenReturn(null);

        when(config.getKeys()).thenReturn(keyConfiguration);

        final Path passwordFile = mock(Path.class);
        when(keyConfiguration.getPasswordFile()).thenReturn(passwordFile);

        when(filesDelegate.notExists(passwordFile)).thenReturn(true);

        when(filesDelegate.setPosixFilePermissions(passwordFile, NEW_PASSWORD_FILE_PERMS)).thenReturn(passwordFile);

        Config result = configParser.doPasswordStuff(config);

        assertThat(result).isSameAs(config);

        verify(filesDelegate).notExists(passwordFile);
        verify(filesDelegate).setPosixFilePermissions(passwordFile, NEW_PASSWORD_FILE_PERMS);
        verify(filesDelegate).createFile(passwordFile);

        verify(filesDelegate).write(passwordFile, Arrays.asList(password), APPEND);

        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void doPasswordStuffNewPasswordsOnly() {
        final String password = "I LOVE SPARROWS!";

        final ConfigKeyPair newKey = mock(ConfigKeyPair.class);
        when(newKey.getPassword()).thenReturn(password);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        final List<ConfigKeyPair> newKeys = Arrays.asList(newKey);

        final ConfigurationParser configParser = new ConfigurationParser(newKeys, filesDelegate);

        Config config = mock(Config.class);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getKeyData()).thenReturn(Collections.emptyList());

        when(keyConfiguration.getPasswords()).thenReturn(null);
        when(keyConfiguration.getPasswordFile()).thenReturn(null);
        when(config.getKeys()).thenReturn(keyConfiguration);

        Throwable ex = catchThrowable(() -> configParser.doPasswordStuff(config));

        assertThat(ex).isInstanceOf(ConfigException.class);
        verifyZeroInteractions(filesDelegate);
    }

    @Test
    public void doPasswordStuffNoNewPasswords() {

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        final ConfigurationParser configParser = new ConfigurationParser(Collections.emptyList(), filesDelegate);

        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getPasswordFile()).thenReturn(null);
        when(keyConfiguration.getPasswords()).thenReturn(null);

        when(config.getKeys()).thenReturn(keyConfiguration);

        Config result = configParser.doPasswordStuff(config);
        assertThat(result).isSameAs(config);
    }
}
