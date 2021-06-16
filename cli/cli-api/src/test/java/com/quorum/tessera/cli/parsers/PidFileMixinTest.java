package com.quorum.tessera.cli.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.io.FilesDelegate;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PidFileMixinTest {

  private PidFileMixin pidFileMixin;

  @Rule public TemporaryFolder dir = new TemporaryFolder();

  private Path pidFile;

  @Before
  public void beforeTest() throws Exception {
    this.pidFileMixin = new PidFileMixin();
    this.pidFile = dir.getRoot().toPath().resolve("PidFile.pid");

    assertThat(pidFile).doesNotExist();
  }

  @Test
  public void afterTest() throws Exception {
    Files.deleteIfExists(pidFile);
  }

  @Test
  public void noPidFilePathDoesNothing() {
    this.pidFileMixin.createPidFile();
    assertThat(pidFile).doesNotExist();
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
