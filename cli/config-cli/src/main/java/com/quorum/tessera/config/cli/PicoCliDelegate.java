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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Objects;
import java.util.Optional;

public class PicoCliDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliDelegate.class);

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
                .setParameterExceptionHandler(mapper);

        commandLine.execute(args);

        // if an exception occurred, throw it to to the upper levels where it gets handled
        if (mapper.getThrown() != null) {
            try {
                throw mapper.getThrown();
            } catch(NoTesseraConfigfileOptionException e) {
                throw new CliException("Missing required option '--configfile <config>'\n" + commandLine.getUsageMessage());
            }
        }
        return (CliResult) Optional.ofNullable(commandLine.getExecutionResult())
            .orElse(new CliResult(0, true, null));
    }
}
