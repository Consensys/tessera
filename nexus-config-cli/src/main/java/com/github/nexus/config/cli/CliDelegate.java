package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigFactory;
import com.github.nexus.config.util.JaxbUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public enum CliDelegate {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegate.class);

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return config;
    }

    public CliResult execute(String... args) throws Exception {

        Options options = new Options();
        options.addOption(
            Option.builder("configfile")
                .desc("Path to node configuration file")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
                .required()
                .build());

        //If keygen then we require the path to the private key config path
        options.addOption(
            Option.builder("keygen")
                .desc("Path to private key config for generation of missing key files")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
                .build());

        options.addOption(
            Option.builder("output")
                .desc("Generate updated config file with generated keys")
                .hasArg(true)
                .numberOfArgs(1)
                .build());

        options.addOption(
            Option.builder("pidfile")
                .desc("Path to pid file")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
                .build());

        if (Arrays.asList(args).contains("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("nexus -configfile <PATH> [-keygen <PATH>] [-pidfile <PATH>]", options);
            return new CliResult(0, true, null);
        }

        CommandLineParser parser = new DefaultParser();

        try {

            CommandLine line = parser.parse(options, args);

            parseConfig(line);

            if (line.hasOption("pidfile")) {
                createPidFile(line);
            }

            return new CliResult(0, false, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

    private void parseConfig(CommandLine commandLine) throws IOException {

        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final ConfigFactory configFactory = ConfigFactory.create();

        final Path path = Paths.get(commandLine.getOptionValue("configfile"));

        if (!Files.exists(path)) {
            throw new FileNotFoundException(String.format("%s not found.", path));
        }

        final List<InputStream> keyGetConfigs = getKeyGenConfig(commandLine);


        try (InputStream in = Files.newInputStream(path)) {
            this.config = configFactory.create(in, keyGetConfigs.toArray(new InputStream[0]));
        }

        Set<ConstraintViolation<Config>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (!keyGetConfigs.isEmpty()) {
            //we have generated new keys, so we need to output the new configuration
            output(commandLine);
        }

    }

    private List<InputStream> getKeyGenConfig(CommandLine commandLine) throws IOException {

        List<InputStream> keyGenConfigs = new ArrayList<>();

        if (commandLine.hasOption("keygen")) {
            String[] keyGenConfigFiles = commandLine.getOptionValues("keygen");

            for (final String pathStr : keyGenConfigFiles) {
                keyGenConfigs.add(
                    Files.newInputStream(
                        Paths.get(pathStr)
                    )
                );
            }
        }

        return keyGenConfigs;
    }

    private void output(CommandLine commandLine) throws IOException {

        if (commandLine.hasOption("output")) {
            final Path outputConfigFile = Paths.get(commandLine.getOptionValue("output"));

            try (OutputStream out = Files.newOutputStream(outputConfigFile, CREATE_NEW)) {
                JaxbUtil.marshal(this.config, out);
            }
        } else {
            JaxbUtil.marshal(this.config, System.out);
        }
    }

    private void createPidFile(CommandLine commandLine) throws IOException {

        final Path pidFilePath = Paths.get(commandLine.getOptionValue("pidfile"));

        if (Files.exists(pidFilePath)) {
            LOGGER.info("File already exists " + pidFilePath);
        } else {
            Files.createFile(pidFilePath);
            LOGGER.info("Creating pid file " + pidFilePath);
        }

        final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        try (final OutputStream stream = Files.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
            stream.write(pid.getBytes(StandardCharsets.UTF_8));
        }
    }

}
