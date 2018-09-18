package com.quorum.tessera.config.cli.parsers;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PidFileParser implements Parser<Optional> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PidFileParser.class);

    @Override
    public Optional parse(final CommandLine commandLine) throws IOException {

        if (commandLine.hasOption("pidfile")) {

            final Path pidFilePath = Paths.get(commandLine.getOptionValue("pidfile"));

            if (Files.exists(pidFilePath)) {
                LOGGER.info("File already exists {}", pidFilePath);
            } else {
                Files.createFile(pidFilePath);
                LOGGER.info("Created pid file {}", pidFilePath);
            }

            final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

            try (final OutputStream stream = Files.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
                stream.write(pid.getBytes(UTF_8));
            }

        }

        return Optional.empty();
    }

}
