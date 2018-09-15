package com.quorum.tessera.config.cli.parsers;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PidFileParserTest {

    private PidFileParser pidFileParser = new PidFileParser();

    private Path pidFile;

    @Before
    public void init() throws IOException {
        this.pidFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    }

    @Test
    public void fileOverwrittenIfAlreadyExists() throws IOException {

        final String message = "THIS ISNT A PID";

        Files.write(this.pidFile, message.getBytes());

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("pidfile")).thenReturn(true);
        when(commandLine.getOptionValue("pidfile")).thenReturn(this.pidFile.toAbsolutePath().toString());

        assertThat(this.pidFile).exists();

        this.pidFileParser.parse(commandLine);

        final byte[] filesBytes = Files.readAllBytes(this.pidFile);

        assertThat(filesBytes).isNotEqualTo(message.getBytes());

    }

    @Test
    public void fileCreatedIfNotAlreadyExists() throws IOException {

        Files.deleteIfExists(this.pidFile);

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("pidfile")).thenReturn(true);
        when(commandLine.getOptionValue("pidfile")).thenReturn(this.pidFile.toAbsolutePath().toString());

        assertThat(this.pidFile).doesNotExist();

        this.pidFileParser.parse(commandLine);

        assertThat(this.pidFile).exists();

    }

}
