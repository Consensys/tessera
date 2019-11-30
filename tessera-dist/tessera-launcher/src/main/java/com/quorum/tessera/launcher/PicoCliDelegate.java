package com.quorum.tessera.launcher;

import com.quorum.tessera.cli.CLIExceptionCapturer;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.KeyUpdateCommand;
import com.quorum.tessera.config.cli.KeyUpdateCommandFactory;
import com.quorum.tessera.config.cli.OverrideUtil;
import com.quorum.tessera.key.generation.KeyGenCommand;
import com.quorum.tessera.key.generation.TesseraCommand;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.Map;

public class PicoCliDelegate {
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

        commandLine.execute(args);

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            throw mapper.getThrown();
        }

        //        try {
        //            CommandLine.ParseResult pr = commandLine.parseArgs(args);
        //            if (CommandLine.printHelpIfRequested(pr)) {
        //                return new CliResult(0, true, null);
        //            }
        //
        //            if (pr.hasSubcommand()) {
        //                CommandLine.ParseResult subPr = pr.subcommand();
        //                CommandSpec subCommand = subPr.commandSpec();
        //
        //                List<String> matchedStr = new ArrayList<>();
        //                List<CommandLine.Model.ArgSpec> matchedArgs = subPr.matchedArgs();
        //
        //                matchedArgs
        //                    .stream()
        //                    .map(CommandLine.Model.ArgSpec::originalStringValues)
        //                    .forEachOrdered(matchedStr::addAll);
        //
        ////                List<CommandLine> commands = subPr.asCommandLineList();
        ////                if (commands.size() != 1) {
        ////                    throw new RuntimeException("at the moment exactly 1 subcommand has to be specified");
        ////                }
        ////
        ////                commands.get(0).execute();
        //
        //                List<String> originalArgs = subPr.originalArgs();
        //
        //                if ("keygen".equals(subCommand.name())) {
        //                    System.out.println("name = " + subCommand.name());
        //                    keyGenCommandLine.execute(originalArgs.toArray(new String[0]));
        //                }
        //            }

        //            int count = pr.matchedOptionValue('c', 1);
        //            List<File> files = pr.matchedPositionalValue(0, Collections.<File>emptyList());
        //            for (File f : files) {
        //                for (int i = 0; i < count; i++) {
        //                    System.out.printf("%d: %s%n", i, f);
        //                }
        //            }
        //        } catch (CommandLine.ParameterException invalidInput) {
        //            System.err.println(invalidInput.getMessage());
        //            invalidInput.getCommandLine().usage(System.err);
        //        }

        return new CliResult(1, true, null);
    }
}
