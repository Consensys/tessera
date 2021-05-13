package com.quorum.tessera.cli.parsers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import com.quorum.tessera.io.FilesDelegate;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class PidFileMixin {

  private static final Logger LOGGER = LoggerFactory.getLogger(PidFileMixin.class);

  private final FilesDelegate filesDelegate;

  @CommandLine.Option(
      names = {"--pidfile", "-pidfile"},
      description = "Create a file at the specified path containing the process' ID (PID)")
  private Path pidFilePath = null;

  public PidFileMixin() {
    this(FilesDelegate.create());
  }

  // package-private for testing
  PidFileMixin(FilesDelegate filesDelegate) {
    this.filesDelegate = filesDelegate;
  }

  public void createPidFile() {
    if (pidFilePath == null) {
      return;
    }

    if (Files.exists(pidFilePath)) {
      LOGGER.info("File already exists {}", pidFilePath);
    } else {
      LOGGER.info("Created pid file {}", pidFilePath);
    }

    final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    try (OutputStream stream =
        filesDelegate.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
      stream.write(pid.getBytes(UTF_8));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public void setPidFilePath(final Path pidFilePath) {
    this.pidFilePath = pidFilePath;
  }
}
