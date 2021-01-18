package com.quorum.tessera.config.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
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

//    private final Validator validator =
//            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final KeyPasswordResolver keyPasswordResolver;

    public PicoCliDelegate() {
        this(ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
    }

    private PicoCliDelegate(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public CliResult execute(String... args) throws Exception {
        LOGGER.debug("Execute with args [{}]", String.join(",", args));
        final CommandLine commandLine = new CommandLine(TesseraCommand.class);

        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();

        commandLine.addSubcommand(new CommandLine(CommandLine.HelpCommand.class));
        commandLine.addSubcommand(new CommandLine(VersionCommand.class));
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

//        final CommandLine.ParseResult parseResult;
//        try {
//            parseResult = commandLine.parseArgs(args);
//        } catch (CommandLine.ParameterException ex) {
//            try {
//                commandLine.getParameterExceptionHandler().handleParseException(ex, args);
//                throw new CliException(ex.getMessage());
//            } catch (Exception e) {
//                throw new CliException(ex.getMessage());
//            }
//        }
//
//        if (CommandLine.printHelpIfRequested(parseResult)) {
//            return new CliResult(0, true, null);
//        }
//
//        if (!parseResult.hasSubcommand()) {
//            // the node is being started
//            final Config config;
//            try {
//                config = getConfigFromCLI(parseResult);
//            } catch (NoTesseraCmdArgsException e) {
//                commandLine.execute("help");
//                return new CliResult(0, true, null);
//            } catch (NoTesseraConfigfileOptionException e) {
//                throw new CliException("Missing required option '--configfile <config>'");
//            }
//            LOGGER.debug("Executed with args [{}]", String.join(",", args));
//            LOGGER.trace("Config {}", JaxbUtil.marshalToString(config));
//            return new CliResult(0, false, config);
//        }
//
//        // there is a subcommand
//
//        // print help if no args provided and not version subcmd
//        if (args.length == 1) {
//            final CommandLine.ParseResult subcmdParseResult = parseResult.subcommand();
//            final CommandLine subcmd = subcmdParseResult.asCommandLineList().get(0);
//            if ("version".equals(subcmd.getCommandName())) {
//                subcmd.printVersionHelp(System.out);
//                return new CliResult(0, true, null);
//            } else {
//                subcmd.execute("help");
//                return new CliResult(0, true, null);
//            }
//        }

        commandLine..execute(args);

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            try {
                throw mapper.getThrown();
            } catch(NoTesseraConfigfileOptionException e) {
                commandLine.execute("help");
                throw new CliException("Missing required option '--configfile <config>'");
            }
        }
        return (CliResult) Optional.ofNullable(commandLine.getExecutionResult())
            .orElse(new CliResult(0, true, null));
    }
}
