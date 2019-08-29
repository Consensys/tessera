package com.quorum.tessera.cli.parsers;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PidFileMixinTest {

    private PidFileMixin pidFileMixin;

    private Path pidFile;

    @Before
    public void init() throws IOException {
        this.pidFileMixin = new PidFileMixin();
        this.pidFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    }

    @Test
    public void noPidFileReturnsEarly() throws Exception {
        final boolean result = this.pidFileMixin.call();

        assertThat(result).isTrue();
    }

    @Test
    public void fileOverwrittenIfAlreadyExists() throws Exception {
        final byte[] message = "THIS ISNT A PID".getBytes();
        Files.write(this.pidFile, message);

        this.pidFileMixin.setPidFilePath(this.pidFile.toAbsolutePath());
        assertThat(this.pidFile).exists();

        this.pidFileMixin.call();

        final byte[] filesBytes = Files.readAllBytes(this.pidFile);
        assertThat(filesBytes).isNotEqualTo(message);
    }

    @Test
    public void fileCreatedIfNotAlreadyExists() throws Exception {
        Files.deleteIfExists(this.pidFile);

        this.pidFileMixin.setPidFilePath(this.pidFile.toAbsolutePath());
        assertThat(this.pidFile).doesNotExist();

        this.pidFileMixin.call();

        assertThat(this.pidFile).exists();
    }
}
