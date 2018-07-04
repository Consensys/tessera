package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigFactory;
import org.apache.commons.cli.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

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
            return new CliResult(0,  null);
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

//            if (line.hasOption("keygen")) {
//
//                Set<ConstraintViolation<Config>> keyGenViolations = validator.validate(config, KeyGen.class);
//                if(!keyGenViolations.isEmpty()) {
//                    throw new ConstraintViolationException(keyGenViolations);
//                }
//
//                KeyGenerator keyGenerator = KeyGeneratorFactory.create();
//
//                config.getKeys().stream()
//                        .forEach(keyGenerator::generate);
//
//                try (final Writer writer = new StringWriter()) {
//
//                    JAXB.marshal(config, writer);
//
//                    final String data = writer.toString();
//
//                    try (Reader reader = new StringReader(data)) {
//                        Config newConfig = JAXB.unmarshal(new StreamSource(reader), Config.class);
//                        return new CliResult(0, newConfig);
//                    }
//                }
//            }

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
