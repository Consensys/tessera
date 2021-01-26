package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.io.FilesDelegate;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PidFileMixinTest {

    private PidFileMixin pidFileMixin;

    private Path pidFile;

    @Before
    public void init() throws IOException {
        this.pidFileMixin = new PidFileMixin();
        this.pidFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    }

    @Test
    public void noPidFilePathDoesNothing() {
        this.pidFileMixin.createPidFile();
    }

    @Test
    public void fileOverwrittenIfAlreadyExists() throws Exception {
        final byte[] message = "THIS ISNT A PID".getBytes();
        Files.write(this.pidFile, message);

        this.pidFileMixin.setPidFilePath(this.pidFile.toAbsolutePath());
        assertThat(this.pidFile).exists();

        this.pidFileMixin.createPidFile();

        final byte[] filesBytes = Files.readAllBytes(this.pidFile);
        assertThat(filesBytes).isNotEqualTo(message);
    }

    @Test
    public void fileCreatedIfNotAlreadyExists() throws Exception {
        Files.deleteIfExists(this.pidFile);

        this.pidFileMixin.setPidFilePath(this.pidFile.toAbsolutePath());
        assertThat(this.pidFile).doesNotExist();

        this.pidFileMixin.createPidFile();

        assertThat(this.pidFile).exists();
    }

    @Test
    public void wrapIOException() throws IOException {
        final IOException ioException = new IOException("some error");

        final FilesDelegate filesDelegate = mock(FilesDelegate.class);
        final OutputStream outputStream = mock(OutputStream.class);
        when(filesDelegate.newOutputStream(any(), any(), any())).thenReturn(outputStream);
        doThrow(ioException).when(outputStream).write(any());

        final PidFileMixin mockablePidFileMixin = new PidFileMixin(filesDelegate);
        mockablePidFileMixin.setPidFilePath(pidFile.toAbsolutePath());

        Throwable ex = catchThrowable(() -> mockablePidFileMixin.createPidFile());
        assertThat(ex).isExactlyInstanceOf(UncheckedIOException.class);
        assertThat(ex).hasCause(ioException);
    }
}
