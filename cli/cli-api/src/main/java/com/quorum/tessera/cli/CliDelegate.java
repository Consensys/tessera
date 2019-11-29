package com.quorum.tessera.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static picocli.CommandLine.Model.CommandSpec.DEFAULT_COMMAND_NAME;

public enum CliDelegate {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegate.class);

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

        if (args.length > 0) {
            String firstArg = args[0];
            if ("admin".equalsIgnoreCase(firstArg)) {
                System.setProperty(CliType.CLI_TYPE_KEY, CliType.ADMIN.name());
            }
        }

        final List<CliAdapter> adapters = ServiceLoaderUtil.loadAll(CliAdapter.class).collect(Collectors.toList());

        LOGGER.debug("Loaded adapters {}", adapters);

        CliType cliType = CliType.valueOf(System.getProperty(CliType.CLI_TYPE_KEY, CliType.CONFIG.name()));

        LOGGER.debug("cliType {}", cliType);

        Predicate<CliAdapter> isTopLevel =
                a -> a.getClass().getAnnotation(CommandLine.Command.class).name().equals(DEFAULT_COMMAND_NAME);

        // Finds the top level adapter that we want to start with. Exactly one is expected to be on the classpath.
        final CliAdapter adapter =
                adapters.stream()
                        .filter(a -> a.getClass().isAnnotationPresent(CommandLine.Command.class))
                        .filter(isTopLevel)
                        .findFirst()
                        .get();

        LOGGER.debug("Loaded adapter {}", adapter);

        // Then we find all the others and attach them as sub-commands. It is expected that they have defined their
        // own hierarchy and command names.
        final List<CliAdapter> subcommands =
                adapters.stream()
                        // .filter(isTopLevel)
                        .filter(a -> a != adapter)
                        .filter(a -> a.getType() != CliType.ENCLAVE)
                        .collect(Collectors.toList());

        // the mapper will give us access to the exception from the outside, if one occurred.
        // mostly since we have an existing system, and this is a workaround
        final CLIExceptionCapturer mapper = new CLIExceptionCapturer();
        final CommandLine commandLine = new CommandLine(adapter);

        subcommands.stream().peek(sc -> LOGGER.debug("Adding subcommand {}", sc)).forEach(commandLine::addSubcommand);

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
