package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
                        .desc("Confguration file path")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required()
                        .build());

        options.addOption(
                Option.builder("keygen")
                        .desc("Create missing ssl key files")
                        .hasArg(false)
                        .build());

        options.addOption(
                Option.builder("pidfile")
                        .desc("Pile file path")
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

        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        try {

            CommandLine line = parser.parse(options, args);

            Path path = Paths.get(line.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = Files.newInputStream(path)) {
                this.config = ConfigFactory.create().create(in);
            }

            Set<ConstraintViolation<Config>> violations = validator.validate(config);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return new CliResult(0, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

}
