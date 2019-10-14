package com.quorum.tessera.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static picocli.CommandLine.Model.CommandSpec.DEFAULT_COMMAND_NAME;

public enum CliDelegate {
    INSTANCE;

    private static final CliResult HELP_RESULT = new CliResult(0, true, null);

    private static final CliResult DEFAULT_RESULT = new CliResult(1, true, null);

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
        final CliResult result =
                commandLine.getParseResult().asCommandLineList().stream()
                        .map(cl -> cl.isUsageHelpRequested() ? HELP_RESULT : cl.getExecutionResult())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(DEFAULT_RESULT);

        this.config = result.getConfig().orElse(null);
        return result;
    }
}
