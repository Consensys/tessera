package com.quorum.tessera.picocli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.OverrideUtil;
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

// TODO(cjh) make sure recent changes to old CLI are included where needed
public class PicoCliDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

    private final Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final KeyPasswordResolver keyPasswordResolver;

    public PicoCliDelegate() {
        this(ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
    }

    private PicoCliDelegate(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public CliResult execute(String... args) throws Exception {
        final CommandSpec command = CommandSpec.forAnnotatedObject(TesseraCommand.class);

        // TODO(cjh) most usage options have empty lines between them, but not all.  Need to remove the empty lines.
        // add config override options, dynamically generated from the config object
//        Map<String, Class> overrideOptions = OverrideUtil.buildConfigOptions();
//        overrideOptions.forEach(
//                (optionName, optionType) -> {
//                    OptionSpec.Builder optionBuilder =
//                            OptionSpec.builder(String.format("--%s", optionName))
//                                    .paramLabel(optionType.getSimpleName())
//                                    .type(optionType);
//
//                    command.addOption(optionBuilder.build());
//                });

        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();

        CommandLine keyGenCommandLine = new CommandLine(KeyGenCommand.class);

        final CommandLine.IFactory keyUpdateCommandFactory = new KeyUpdateCommandFactory();
        CommandLine keyUpdateCommandLine = new CommandLine(KeyUpdateCommand.class, keyUpdateCommandFactory);

        command.addSubcommand(null, new CommandLine(CommandLine.HelpCommand.class));
        command.addSubcommand(null, keyGenCommandLine);
        command.addSubcommand(null, keyUpdateCommandLine);

        final CommandLine commandLine = new CommandLine(command);
        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .registerConverter(ArgonOptions.class, new ArgonOptionsConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(mapper)
                .setParameterExceptionHandler(mapper);

        final CommandLine.ParseResult parseResult;
        try {
            parseResult = commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException ex) {
            //             TODO(cjh) this is ripped from commandLine.execute(...) - check whether it is sufficient, or
            // if it can be replaced by using the mapper
            // exception mapper can't be used here as we haven't called commandLine.execute()
            try {
                commandLine.getParameterExceptionHandler().handleParseException(ex, args);
                throw new CliException(ex.getMessage());
            } catch (Exception e) {
                throw new CliException(ex.getMessage());
            }
        }

        if (CommandLine.printHelpIfRequested(parseResult)) {
            return new CliResult(0, true, null);
        }

        if (!parseResult.hasSubcommand()) {
            // the node is being started
            final Config config;
            try {
                config = getConfigFromCLI(parseResult);
            } catch (NoTesseraCmdArgsException e) {
                commandLine.execute("help");
                return new CliResult(0, true, null);
            } catch (NoTesseraConfigfileOptionException e) {
                throw new CliException("Missing required option '--configfile <config>'");
            }

            return new CliResult(0, false, config);

        } else {
            // there is a subcommand
            CommandLine.ParseResult subParseResult = parseResult.subcommand();

            String[] subCmdAndArgs = subParseResult.originalArgs().toArray(new String[0]);

            // print help as no args provided
            if (subCmdAndArgs.length == 1) {
                subParseResult.asCommandLineList().get(0).execute("help");
                return new CliResult(0, true, null);
            }

            String[] subArgs = new String[subCmdAndArgs.length - 1];
            System.arraycopy(subCmdAndArgs, 1, subArgs, 0, subArgs.length);

            // TODO(cjh) document the change of behaviour meaning node cannot start after keygen
            subParseResult.asCommandLineList().get(0).execute(subArgs);

            // if an exception occurred, throw it to to the upper levels where it gets handled
            if (mapper.getThrown() != null) {
                throw mapper.getThrown();
            }

            return new CliResult(0, true, null);
        }
    }

    private Config getConfigFromCLI(CommandLine.ParseResult parseResult) throws Exception {
        List<CommandLine.Model.ArgSpec> parsedArgs = parseResult.matchedArgs();

        if (parsedArgs.size() == 0) {
            throw new NoTesseraCmdArgsException();
        }

        final Config config;

        // start with any config read from the file
        if (parseResult.hasMatchedOption("configfile")) {
            config = parseResult.matchedOption("configfile").getValue();
        } else {
            throw new NoTesseraConfigfileOptionException();
        }

        if (parseResult.hasMatchedOption("override")) {
            Map<String, String> overrides = parseResult.matchedOption("override").getValue();

            for (String target : overrides.keySet()) {
                String value = overrides.get(target);

                // apply CLI overrides
                LOGGER.debug("Setting : {} with value(s) {}", target, value);
                OverrideUtil.setValue(config, target, value);
                LOGGER.debug("Set : {} with value(s) {}", target, value);
            }
        }

        keyPasswordResolver.resolveKeyPasswords(config);

        final Set<ConstraintViolation<Config>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (parseResult.hasMatchedOption("pidfile")) {
            createPidFile(parseResult.matchedOption("pidfile").getValue());
        }

        return config;
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
