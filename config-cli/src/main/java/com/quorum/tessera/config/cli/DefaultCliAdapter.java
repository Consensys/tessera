package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.keys.KeyGenerator;
import com.quorum.tessera.config.keys.KeyGeneratorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Collections.singletonList;
import javax.validation.Validator;
import javax.validation.*;

public class DefaultCliAdapter implements CliAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCliAdapter.class);

    private final KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.newFactory();

    private final Validator validator = Validation.byDefaultProvider()
            .configure()
            .ignoreXmlConfiguration()
            .buildValidatorFactory()
            .getValidator();

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
                        .build());

        //If keygen then we require the path to the private key config path
        options.addOption(
                Option.builder("keygen")
                        .desc("Use this option to generate public/private keypair")
                        .hasArg(false)
                        .build());

        options.addOption(
                Option.builder("filename")
                        .desc("Path to private key config for generation of missing key files")
                        .hasArg(true)
                        .optionalArg(false)
                        .argName("PATH")
                        .build());

        options.addOption(
                Option.builder("keygenconfig")
                        .desc("Path to private key config for generation of missing key files")
                        .hasArg(true)
                        .optionalArg(false)
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
                        .argName(optionType.getSimpleName().toUpperCase() + "...");
            } else {
                optionBuilder.hasArg()
                        .argName(optionType.getSimpleName().toUpperCase());
            }

            if(!optionName.startsWith("keys") && !optionName.startsWith("alwaysSendTo")) {
                options.addOption(optionBuilder.build());
            }

        });

        final List<String> argsList = Arrays.asList(args);
        if (argsList.contains("help") || argsList.isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(200);
            formatter.printHelp("tessera -configfile <PATH> [-keygen <PATH>] [-pidfile <PATH>]", options);
            return new CliResult(0, true, false, null);
        }

        final CommandLineParser parser = new DefaultParser();

        try {

            final CommandLine line = parser.parse(options, args);

            final Config config = parseConfig(line);

            overrideOptions.entrySet().forEach(dynEntry -> {
                String optionName = dynEntry.getKey();
                if (line.hasOption(optionName)) {
                    String[] values = line.getOptionValues(optionName);
                    LOGGER.debug("Setting : {} with value(s) {}", optionName, values);
                    OverrideUtil.setValue(config, optionName, values);
                    LOGGER.debug("Set : {} with value(s) {}", optionName, values);
                }
            });

            if (Objects.nonNull(config)) {

                Set<ConstraintViolation<Config>> violations = validator.validate(config);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }
            }

            if (line.hasOption("pidfile")) {
                createPidFile(line);
            }

            return new CliResult(0, false, line.hasOption("keygen"), config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

    private Config parseConfig(CommandLine commandLine) throws IOException {

        final ConfigFactory configFactory = ConfigFactory.create();

        final List<String> keyGenConfigs = this.getKeyGenConfig(commandLine);
        final ArgonOptions options = this.keygenConfiguration(commandLine).orElse(null);

        Config config = null;

        if (commandLine.hasOption("configfile")) {
            final Path path = Paths.get(commandLine.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = Files.newInputStream(path)) {
                config = configFactory.create(in, options, keyGenConfigs.toArray(new String[0]));
            }

            if (!keyGenConfigs.isEmpty()) {
                //we have generated new keys, so we need to output the new configuration
                output(commandLine, config);
            }

        } else if(commandLine.hasOption("keygen")) {
            final KeyGenerator generator = keyGeneratorFactory.create();
            keyGenConfigs
                    .stream()
                    .map(name -> generator.generate(name, options))
                    .collect(Collectors.toList());
        } else {
            throw new CliException("One or both: -configfile <PATH> or -keygen options are required.");
        }

        return config;
    }

    private List<String> getKeyGenConfig(CommandLine commandLine) {

        if (commandLine.hasOption("keygen")) {

            if (commandLine.hasOption("filename")) {

                final String keyNames = commandLine.getOptionValue("filename");
                if (keyNames != null) {
                    return Stream.of(keyNames.split(",")).collect(Collectors.toList());
                }

            }

            return singletonList("");
        }

        return new ArrayList<>();
    }

    private static Optional<ArgonOptions> keygenConfiguration(final CommandLine commandLine) throws IOException {
        if (commandLine.hasOption("keygenconfig")) {
            final String pathName = commandLine.getOptionValue("keygenconfig");
            final InputStream configStream = Files.newInputStream(Paths.get(pathName));

            ArgonOptions argonOptions = JaxbUtil.unmarshal(configStream, ArgonOptions.class);
            return Optional.of(argonOptions);
        }
        return Optional.empty();
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
