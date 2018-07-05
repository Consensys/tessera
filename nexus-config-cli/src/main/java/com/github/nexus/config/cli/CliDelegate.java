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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        //If keygen then we require the path to the private key config path
        options.addOption(
                Option.builder("keygen")
                        .desc("Create missing ssl key files")
                        .hasArg(true)
                        .numberOfArgs(1)
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

        final ConfigFactory configFactory = ConfigFactory.create();

        try {

            CommandLine line = parser.parse(options, args);

            Path path = Paths.get(line.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            List<InputStream> keyGetConfigs = new ArrayList<>();
            if (line.hasOption("keygen")) {
                String[] keyGenConfigFiles = line.getOptionValues("keygen");

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

            if(!keyGetConfigs.isEmpty()) {
                //we have generated new keys, so we need to output the new configuration
                System.out.println(JaxbUtil.marshalToString(this.config));
            }

            return new CliResult(0, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

}
