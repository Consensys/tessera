package com.quorum.tessera.launcher;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.KeyUpdateCommand;
import com.quorum.tessera.config.cli.KeyUpdateCommandFactory;
import com.quorum.tessera.config.cli.OverrideUtil;
import com.quorum.tessera.key.generation.KeyGenCommand;
import com.quorum.tessera.key.generation.TesseraCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PicoCliDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

    private final Validator validator =
        Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final KeyPasswordResolver keyPasswordResolver;

    public PicoCliDelegate() {
        this(ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
    }

    public PicoCliDelegate(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public CliResult execute(String[] args) throws Exception {
        final CommandSpec command = CommandSpec.forAnnotatedObject(TesseraCommand.class);

        // TODO(cjh) add options and positional parameters
        // TODO(cjh) most usage options have empty lines between them, but not all.  Need to remove the empty lines.
        // add config override options, dynamically generated from the config object
        Map<String, Class> overrideOptions = OverrideUtil.buildConfigOptions();
        overrideOptions.forEach(
                (optionName, optionType) -> {
                    OptionSpec.Builder optionBuilder =
                            OptionSpec.builder(String.format("--%s", optionName))
                                    .paramLabel(optionType.getSimpleName())
                                    .type(optionType);

                    command.addOption(optionBuilder.build());
                });

        command.addSubcommand(null, new CommandLine(CommandLine.HelpCommand.class));
        command.addSubcommand(null, new CommandLine(KeyGenCommand.class));

        final CommandLine.IFactory keyUpdateCommandFactory = new KeyUpdateCommandFactory();
        command.addSubcommand(null, new CommandLine(KeyUpdateCommand.class, keyUpdateCommandFactory));

        final CommandLine commandLine = new CommandLine(command);
        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();
        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(mapper)
                .setParameterExceptionHandler(mapper);

        final CommandLine.ParseResult parseResult;
        try {
            parseResult = commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException ex) {
//             TODO(cjh) this is ripped from commandLine.execute(...) - check whether it is sufficient, or if it can be replaced by using the mapper
            try {
                int exitCode = commandLine.getParameterExceptionHandler().handleParseException(ex, args);
                return new CliResult(exitCode, true, null);
            } catch (Exception e) {
                throw e;
            }
        }

        if (CommandLine.printHelpIfRequested(parseResult)) {
            return new CliResult(0, true, null);
        }

        if (!parseResult.hasSubcommand()) {
            // the node is being started
            if (parseResult.originalArgs().size() == 0) {
                System.out.println("no options were provided"); // TODO(cjh) delete
                commandLine.execute("help");
            } else {
                System.out.println("at least one option was provided"); // TODO(cjh) delete
                List<CommandLine.Model.ArgSpec> parsedArgs = parseResult.matchedArgs();

                final Config config;

                // start with any config read from the file
                if (parseResult.hasMatchedOption("configfile")) {
                    config = parseResult.matchedOption("configfile").getValue();
                } else {
                    config = new Config();
                }

                // apply CLI overrides
                parsedArgs.forEach(
                    parsedArg -> {
                        // positional (i.e. unnamed) CLI flags are ignored
                        if (!parsedArg.isOption()) {
                            return;
                        }

                        OptionSpec parsedOption = (OptionSpec) parsedArg;

                        // configfile CLI option is ignored as it was already parsed
                        // pidfile CLI option is ignored as it is parsed later
                        // TODO(cjh) improve, checks all names for all provided options
                        for (String name : parsedOption.names()) {
                            if ("--configfile".equals(name) || "--pidfile".equals(name)) {
                                return;
                            }
                        }

                        String optionName = parsedOption.longestName().replaceFirst("^--", "");
                        String[] values = parsedOption.stringValues().toArray(new String[0]);

                        LOGGER.debug("Setting : {} with value(s) {}", optionName, values);
                        OverrideUtil.setValue(config, optionName, values);
                        LOGGER.debug("Set : {} with value(s) {}", optionName, values);
                    });

                System.out.println("all args parsed"); // TODO(cjh) delete
                System.out.println("keys count = " + config.getKeys().getKeyData().size());
                System.out.println("useWhiteList = " + config.isUseWhiteList());

                keyPasswordResolver.resolveKeyPasswords(config);

                final Set<ConstraintViolation<Config>> violations = validator.validate(config);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                if (parseResult.hasMatchedOption("pidfile")) {
                    createPidFile(parseResult.matchedOption("pidfile").getValue());
                }

                return new CliResult(0, false, config);
            }
        } else {
            // there is a subcommand

            parseResult.subcommand();

            // TODO(cjh)
        }

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            throw mapper.getThrown();
        }

        return new CliResult(1, true, null);
    }

    private void createPidFile(Path pidFilePath) throws Exception {
        if (pidFilePath == null) {
            return;
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
    }

}
