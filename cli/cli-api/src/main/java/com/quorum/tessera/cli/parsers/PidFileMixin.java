package com.quorum.tessera.cli.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PidFileMixin implements Callable<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PidFileMixin.class);

    @CommandLine.Option(names = "-pidfile", arity = "1", description = "the path to write the PID to")
    private Path pidFilePath = null;

    @Override
    public Boolean call() throws Exception {
        if (pidFilePath == null) {
            return true;
        }

        if (Files.exists(pidFilePath)) {
            LOGGER.info("File already exists {}", pidFilePath);
        } else {
            LOGGER.info("Created pid file {}", pidFilePath);
        }

        final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        try (OutputStream stream = Files.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
            stream.write(pid.getBytes(UTF_8));
        }

        return true;
    }

    public void setPidFilePath(final Path pidFilePath) {
        this.pidFilePath = pidFilePath;
    }
}
