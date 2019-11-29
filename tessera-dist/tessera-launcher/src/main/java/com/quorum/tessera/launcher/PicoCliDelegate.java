package com.quorum.tessera.launcher;

import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.OverrideUtil;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PicoCliDelegate {
    public CliResult execute(String[] args) {
        final CommandSpec command = CommandSpec.create();

        // TODO(cjh) add options and positional parameters
        //        Options options = this.buildBaseOptions();

        Map<String, Class> overrideOptions = OverrideUtil.buildConfigOptions();

        command.usageMessage()
                .headerHeading("Usage:%n%n")
                .header("Tessera private transaction manager for Quorum")
                .synopsisHeading("%n")
                .descriptionHeading("%nDescription:%n%n")
                .description("Commands to start a Tessera node or manage Tessera encryption keys")
                .optionListHeading("%nOptions:%n")
                .parameterListHeading("%nParameters:%n")
                .abbreviateSynopsis(true);

        // TODO(cjh) most usage options have empty lines between them, but not all.  Need to remove the empty lines.
        overrideOptions.forEach(
                (optionName, optionType) -> {
                    OptionSpec.Builder optionBuilder =
                            OptionSpec.builder(String.format("--%s", optionName))
                                    .paramLabel(optionType.getSimpleName())
                                    .type(optionType);

//                    final boolean isCollection = optionType.isArray();
//                    if (isCollection) {
//                        optionBuilder
//                            .type(List.class)
//                            .auxiliaryTypes(optionType);
//                    } else {
//                        optionBuilder
//                            .type(optionType);
//                    }

                    command.addOption(optionBuilder.build());
                });

        command.addSubcommand(null, new CommandLine(CommandLine.HelpCommand.class));

        final CommandLine commandLine = new CommandLine(command);
        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();
        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(mapper)
                .setParameterExceptionHandler(mapper);
        try {
            CommandLine.ParseResult pr = commandLine.parseArgs(args);
            if (CommandLine.printHelpIfRequested(pr)) {
                return new CliResult(0, true, null);
            }
            int count = pr.matchedOptionValue('c', 1);
            List<File> files = pr.matchedPositionalValue(0, Collections.<File>emptyList());
            for (File f : files) {
                for (int i = 0; i < count; i++) {
                    System.out.printf("%d: %s%n", i, f);
                }
            }
        } catch (CommandLine.ParameterException invalidInput) {
            System.err.println(invalidInput.getMessage());
            invalidInput.getCommandLine().usage(System.err);
        }

        return new CliResult(1, true, null);
    }
}
