package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationParserTest {

    private ConfigurationParser configParser;
    private CommandLine commandLine;

    @Before
    public void setUp() {
        configParser = new ConfigurationParser();
        commandLine = mock(CommandLine.class);
    }

    @Test
    public void providingKeygenAndVaultOptionsThenConfigfileNotParsed() throws Exception {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);

        Config result = configParser.parse(commandLine);

        assertThat(result).isNull();
    }

    @Test
    public void providingKeygenOptionThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");

        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        //FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void providingVaultOptionThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");

        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        //FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void providingNeitherKeygenOptionsThenConfigfileIsParsed() {
        when(commandLine.hasOption("configfile")).thenReturn(true);
        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);

        when(commandLine.getOptionValue("configfile")).thenReturn("some/path");

        Throwable throwable = catchThrowable(() -> configParser.parse(commandLine));

        //FileNotFoundException thrown as "some/path" does not exist
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

}
