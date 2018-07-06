package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigFactory;
import com.github.nexus.config.util.JaxbUtil;
import org.apache.commons.cli.*;

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
import java.util.*;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public enum CliDelegate {

    INSTANCE;

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
                .desc("Configuration file path")
                .hasArg(true)
                .numberOfArgs(1)
                .required()
                .build());

        //If keygen then we require the path to the private key config path
        options.addOption(
            Option.builder("keygen")
                .desc("Create missing key files")
                .hasArg(true)
                .numberOfArgs(1)
                .build());

        options.addOption(
            Option.builder("pidfile")
                .desc("Pid file path")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .build());

        if (Arrays.asList(args).contains("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("nexus", options);
            return new CliResult(0, null);
        }

        CommandLineParser parser = new DefaultParser();

        try {

            CommandLine line = parser.parse(options, args);

            parseConfig(line);

            if (line.hasOption("pidfile")) {
                createPidFile(line);
            }

            return new CliResult(0, config);

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

        List<InputStream> keyGetConfigs = new ArrayList<>();

        if (commandLine.hasOption("keygen")) {
            String[] keyGenConfigFiles = commandLine.getOptionValues("keygen");

            for (final String pathStr : keyGenConfigFiles) {
                keyGetConfigs.add(
                    Files.newInputStream(
                        Paths.get(pathStr)
                    )
                );
            }
        }

        try (InputStream in = Files.newInputStream(path)) {
            this.config = configFactory.create(in, keyGetConfigs.toArray(new InputStream[0]));
        }

        Set<ConstraintViolation<Config>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (!keyGetConfigs.isEmpty()) {
            //we have generated new keys, so we need to output the new configuration
            JaxbUtil.marshal(this.config, System.out);
        }

    }

    private void createPidFile(CommandLine commandLine) throws IOException {

        final Path pidFilePath = Paths.get(commandLine.getOptionValue("pidfile"));

        if (Objects.nonNull(pidFilePath)) {

            if (Files.exists(pidFilePath)) {
                System.out.println("File already exists " + pidFilePath);
            } else {
                Files.createFile(pidFilePath);
                System.out.println("Creating pid file " + pidFilePath);
            }

            final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

            try (final OutputStream stream = Files.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
                stream.write(pid.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}
