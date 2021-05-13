package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.Config;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class PicoCliDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

  public CliResult execute(String... args) throws Exception {
    LOGGER.debug("Execute with args [{}]", String.join(",", args));
    final CommandLine commandLine = new CommandLine(TesseraCommand.class);

    final CLIExceptionCapturer exceptionCapturer = new CLIExceptionCapturer();

    commandLine.addSubcommand(new CommandLine(CommandLine.HelpCommand.class));
    commandLine.addSubcommand(new CommandLine(VersionCommand.class));
    commandLine.addSubcommand(new CommandLine(KeyGenCommand.class, new KeyGenCommandFactory()));
    commandLine.addSubcommand(
        new CommandLine(KeyUpdateCommand.class, new KeyUpdateCommandFactory()));

    commandLine
        .registerConverter(Config.class, new ConfigConverter())
        .registerConverter(ArgonOptions.class, new ArgonOptionsConverter())
        .setSeparator(" ")
        .setCaseInsensitiveEnumValuesAllowed(true)
        .setExecutionExceptionHandler(exceptionCapturer)
        .setParameterExceptionHandler(exceptionCapturer)
        .setStopAtUnmatched(false);

    try {
      // parse the args so that we can print usage help if no cmd args were provided
      final CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
      final List<CommandLine> l = parseResult.asCommandLineList();
      final CommandLine lastCmd = l.get(l.size() - 1);

      // print help if no args were provided
      if (lastCmd.getParseResult().matchedArgs().size() == 0
          && !"help".equals(lastCmd.getCommandName())
          && !"version".equals(lastCmd.getCommandName())) {
        lastCmd.usage(lastCmd.getOut());
      } else {
        commandLine.execute(args);
      }
    } catch (CommandLine.ParameterException ex) {
      exceptionCapturer.handleParseException(ex, args);
    }

    // if an exception occurred, throw it to to the upper levels where it gets handled
    if (exceptionCapturer.getThrown() != null) {
      throw exceptionCapturer.getThrown();
    }
    return (CliResult)
        Optional.ofNullable(commandLine.getExecutionResult()).orElse(new CliResult(0, true, null));
  }
}
