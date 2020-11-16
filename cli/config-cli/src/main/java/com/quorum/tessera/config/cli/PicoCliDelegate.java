package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.reflect.ReflectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PicoCliDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

    private final Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final KeyPasswordResolver keyPasswordResolver;

    public PicoCliDelegate() {
        this(KeyPasswordResolver.create());
    }

    protected PicoCliDelegate(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public CliResult execute(String... args) throws Exception {
        try {
            return doExecute(args);
        } catch (Throwable ex) {
            ex.getCause().printStackTrace();
            throw ex;
        }
    }

    public CliResult doExecute(String... args) throws Exception {
        LOGGER.debug("Execute with args [{}]", String.join(",", args));
        final CommandLine commandLine = new CommandLine(TesseraCommand.class);

        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();

        commandLine.addSubcommand(new CommandLine(CommandLine.HelpCommand.class));
        commandLine.addSubcommand(new CommandLine(KeyGenCommand.class, new KeyGenCommandFactory()));
        commandLine.addSubcommand(new CommandLine(KeyUpdateCommand.class, new KeyUpdateCommandFactory()));

        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .registerConverter(ArgonOptions.class, new ArgonOptionsConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(mapper)
                .setParameterExceptionHandler(mapper)
                .setStopAtUnmatched(false);

        final CommandLine.ParseResult parseResult;
        try {
            parseResult = commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException ex) {
            LOGGER.trace("",ex);
            try {
                commandLine.getParameterExceptionHandler().handleParseException(ex, args);
                throw new CliException(ex.getMessage());
            } catch (Exception e) {
                LOGGER.trace("",e);
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
            LOGGER.debug("Executed with args [{}]", String.join(",", args));
            LOGGER.trace("Config {}", JaxbUtil.marshalToString(config));
            return new CliResult(0, false, config);
        }

        // there is a subcommand

        // print help as no args provided
        if (args.length == 1) {
            final CommandLine.ParseResult subcommand = parseResult.subcommand();
            subcommand.asCommandLineList().get(0).execute("help");
            return new CliResult(0, true, null);
        }

        commandLine.execute(args);

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            throw mapper.getThrown();
        }
        return new CliResult(0, true, null);
    }

    private Config getConfigFromCLI(CommandLine.ParseResult parseResult) throws Exception {
        List<CommandLine.Model.ArgSpec> parsedArgs = parseResult.matchedArgs();

        if (parsedArgs.isEmpty()) {
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

        if (parseResult.hasMatchedOption("recover")) {
            config.setRecoveryMode(true);
        }

        if (Objects.nonNull(parseResult.unmatched())) {
            List<String> unmatched = new ArrayList<>(parseResult.unmatched());

            for (int i = 0; i < unmatched.size(); i++) {
                String line = unmatched.get(i);
                if (line.startsWith("-")) {
                    final String name = line.replaceFirst("-{1,2}", "");
                    final int nextIndex = i + 1;
                    if (nextIndex > (unmatched.size() - 1)) {
                        break;
                    }
                    i = nextIndex;
                    final String value = unmatched.get(nextIndex);
                    try {
                        OverrideUtil.setValue(config, name, value);
                    } catch (ReflectException ex) {
                        // Ignore error
                        LOGGER.debug("", ex);
                    }
                }
            }
        }

        final Set<ConstraintViolation<Config>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        keyPasswordResolver.resolveKeyPasswords(config);

        if (parseResult.hasMatchedOption("pidfile")) {
            createPidFile(parseResult.matchedOption("pidfile").getValue());
        }

        return config;
    }

    private void createPidFile(Path pidFilePath) throws Exception {
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
