package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;

public class PicoCliDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

    public CliResult execute(String... args) throws Exception {
        LOGGER.debug("Execute with args [{}]", String.join(",", args));
        final CommandLine commandLine = new CommandLine(TesseraCommand.class);

        final CLIExceptionCapturer exceptionCapturer = new CLIExceptionCapturer();

        commandLine.addSubcommand(new CommandLine(CommandLine.HelpCommand.class));
        commandLine.addSubcommand(new CommandLine(VersionCommand.class));
        commandLine.addSubcommand(new CommandLine(KeyGenCommand.class, new KeyGenCommandFactory()));
        commandLine.addSubcommand(new CommandLine(KeyUpdateCommand.class, new KeyUpdateCommandFactory()));

        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .registerConverter(ArgonOptions.class, new ArgonOptionsConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(exceptionCapturer)
                .setParameterExceptionHandler(exceptionCapturer);

//        CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
//        final boolean argsContainsHelp = Arrays.asList(args).contains("help");
//        if (parseResult.matchedArgs().size() == 0 && !argsContainsHelp) {
//            CommandLine.ParseResult pr = parseResult;
//
//            while (pr.hasSubcommand()) {
//                pr = pr.subcommand();
//            }
//            pr.asCommandLineList().get(0).usage(commandLine.getOut());
//        } else {
            commandLine.execute(args);
//        }

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (exceptionCapturer.getThrown() != null) {
            throw exceptionCapturer.getThrown();
        }
        return (CliResult) Optional.ofNullable(commandLine.getExecutionResult())
            .orElse(new CliResult(0, true, null));
    }
}
