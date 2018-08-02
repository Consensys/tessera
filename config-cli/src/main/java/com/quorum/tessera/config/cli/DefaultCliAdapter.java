package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCliAdapter implements CliAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCliAdapter.class);

    @Override
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

        Map<String, Class> overrideOptions = OverrideUtil.buildConfigOptions();

        overrideOptions.entrySet().forEach(entry -> {

            final String optionName = entry.getKey();

            final boolean isCollection = entry.getValue().isArray();

            Class optionType = entry.getValue();

            Option.Builder optionBuilder = Option.builder()
                    .longOpt(optionName)
                    .desc(String.format("Override option for %s , type: %s", optionName, optionType.getSimpleName()));

            if (isCollection) {
                optionBuilder.hasArgs()
                        .argName(optionType.getSimpleName().toUpperCase() +"...");
            } else {
                optionBuilder.hasArg()
                        .argName(optionType.getSimpleName().toUpperCase());
            }
            options.addOption(optionBuilder.build());

        });

        if (Arrays.asList(args).contains("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tessera -configfile <PATH> [-keygen <PATH>] [-pidfile <PATH>]", options);
            return new CliResult(0, true, null);
        }

        final CommandLineParser parser = new DefaultParser();

        try {

            final CommandLine line = parser.parse(options, args);
            
            final Config config = parseConfig(line);

            
            if (line.hasOption("pidfile")) {
                createPidFile(line);
            }

            return new CliResult(0, false, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

    private Config parseConfig(CommandLine commandLine) throws IOException {

        final Validator validator = Validation.byDefaultProvider()
                .configure()
                .ignoreXmlConfiguration()
                .buildValidatorFactory()
                .getValidator();

        final ConfigFactory configFactory = ConfigFactory.create();

        final Path path = Paths.get(commandLine.getOptionValue("configfile"));

        if (!Files.exists(path)) {
            throw new FileNotFoundException(String.format("%s not found.", path));
        }

        final List<InputStream> keyGenConfigs = getKeyGenConfig(commandLine);

        final Config config;
        try (InputStream in = Files.newInputStream(path)) {
            config = configFactory.create(in, keyGenConfigs.toArray(new InputStream[0]));
        }

        Set<ConstraintViolation<Config>> violations = validator.validate(config);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (!keyGenConfigs.isEmpty()) {
            //we have generated new keys, so we need to output the new configuration
            output(commandLine, config);
        }
        return config;

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

    private static void output(CommandLine commandLine, Config config) throws IOException {

        if (commandLine.hasOption("output")) {
            final Path outputConfigFile = Paths.get(commandLine.getOptionValue("output"));

            try (OutputStream out = Files.newOutputStream(outputConfigFile, CREATE_NEW)) {
                JaxbUtil.marshal(config, out);
            }
        } else {
            JaxbUtil.marshal(config, System.out);
        }
    }

    private static void createPidFile(CommandLine commandLine) throws IOException {

        final Path pidFilePath = Paths.get(commandLine.getOptionValue("pidfile"));

        if (Files.exists(pidFilePath)) {
            LOGGER.info("File already exists {}", pidFilePath);
        } else {
            Files.createFile(pidFilePath);
            LOGGER.info("Created pid file {}", pidFilePath);
        }

        final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        try (final OutputStream stream = Files.newOutputStream(pidFilePath, CREATE, TRUNCATE_EXISTING)) {
            stream.write(pid.getBytes(StandardCharsets.UTF_8));
        }
    }

}
