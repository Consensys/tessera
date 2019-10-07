package com.quorum.tessera.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static picocli.CommandLine.Model.CommandSpec.DEFAULT_COMMAND_NAME;

public enum CliDelegate {
    INSTANCE;

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return Optional.ofNullable(config)
                .orElseThrow(
                        () -> new IllegalStateException("Execute must be invoked before attempting to fetch config"));
    }

    public CliResult execute(String... args) throws Exception {

        final List<CliAdapter> adapters = ServiceLoaderUtil.loadAll(CliAdapter.class).collect(Collectors.toList());

        // Finds the top level adapter that we want to start with. Exactly one is expected to be on the classpath.
        final CliAdapter adapter =
                adapters.stream()
                        .filter(a -> a.getClass().isAnnotationPresent(CommandLine.Command.class))
                        .filter(
                                a ->
                                        a.getClass()
                                                .getAnnotation(CommandLine.Command.class)
                                                .name()
                                                .equals(DEFAULT_COMMAND_NAME))
                        .findFirst()
                        .get();

        // Then we find all the others and attach them as sub-commands. It is expected that they have defined their
        // own hierarchy and command names.
        final List<CliAdapter> others = new ArrayList<>(adapters);
        others.remove(adapter);

        // the mapper will give us access to the exception from the outside, if one occurred.
        // mostly since we have an existing system, and this is a workaround
        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();
        final CommandLine commandLine = new CommandLine(adapter);
        others.forEach(commandLine::addSubcommand);

        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setUnmatchedArgumentsAllowed(true)
                .setExecutionExceptionHandler(mapper)
                .setParameterExceptionHandler(mapper);

        commandLine.execute(args);

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            throw mapper.getThrown();
        }

        // otherwise, set the config object (if there is one) and return
        final CliResult result = this.getResult(commandLine.getParseResult());
        this.config = result.getConfig().orElse(null);
        return result;
    }

    // checks all the commands thats ran for a result, and returns it, or null otherwise
    public CliResult getResult(final CommandLine.ParseResult parseResult) {
        if (parseResult == null) {
            // we've reached the end of the line and still not found our result,
            // don't start the app and let the console output show the user what happened

            // note: this is a normal case when required options are not given.
            // exit code of 1 is a generic error code, specific exceptions are handled in the Main method
            return new CliResult(1, true, null);
        }

        final CliResult result = parseResult.commandSpec().commandLine().getExecutionResult();
        if (result != null) {
            return result;
        }

        // we used the help option, let the application exit gracefully
        if (parseResult.isUsageHelpRequested()) {
            return new CliResult(0, true, null);
        }

        return getResult(parseResult.subcommand());
    }
}
