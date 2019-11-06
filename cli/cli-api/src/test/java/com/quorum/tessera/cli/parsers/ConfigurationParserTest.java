package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationParserTest {

    private CommandLine commandLine;

    @Before
    public void setUp() {
        commandLine = mock(CommandLine.class);
    }

    @Test
    public void noConfigfileOptionThenDoNothing() throws Exception {
        when(commandLine.hasOption("configfile")).thenReturn(false);
        ConfigurationParser configParser = new ConfigurationParser(Collections.EMPTY_LIST);
        Config result = configParser.parse(commandLine);

        assertThat(result).isNull();
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

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey));
        Config result = configParser.parse(commandLine);

        assertThat(result).isNotNull();
        assertThat(result.getKeys().getKeyData()).contains(newKey);
    }

    @Test
    public void withNewKeysAndOutputOptionWritesNewConfigToFile() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        Path output = Paths.get("target", UUID.randomUUID().toString() + ".conf");

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
    public void withNewKeysAndNullKeyConfig() throws Exception {

        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config-no-keyconfig.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        Path output = Paths.get("target", UUID.randomUUID().toString() + ".conf");

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
    public void withNewKeysAndPasswordFile() throws Exception {
        Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

        configFile.toFile().deleteOnExit();

        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.getOptionValue("configfile")).thenReturn(configFile.toString());

        when(commandLine.hasOption("output")).thenReturn(true);

        Path output = Paths.get("target", UUID.randomUUID().toString() + ".conf");

        when(commandLine.getOptionValue("output")).thenReturn(output.toString());

        ConfigKeyPair newKey = new DirectKeyPair("pub", "priv");

        ConfigurationParser configParser = new ConfigurationParser(Arrays.asList(newKey));

        Config result = configParser.parse(commandLine);

        assertThat(result).isNotNull();
        assertThat(result.getKeys().getKeyData()).contains(newKey);

        assertThat(output).exists();
        output.toFile().deleteOnExit();
    }
}
