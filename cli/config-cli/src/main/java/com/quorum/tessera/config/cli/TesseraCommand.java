package com.quorum.tessera.config.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
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
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@CommandLine.Command(
        name = "tessera",
        header = "Tessera - Privacy Manager for Quorum%n",
        descriptionHeading = "%nDescription:%n",
        description = "Start a Tessera node.  Subcommands exist to manage Tessera encryption keys%n",
        parameterListHeading = "Parameters:%n",
        commandListHeading = "%nCommands:%n",
        optionListHeading = "Options:%n",
        abbreviateSynopsis = true)
public class TesseraCommand implements Callable<CliResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraCommand.class);

    private final Validator validator;

    private final KeyPasswordResolver keyPasswordResolver;

    public TesseraCommand() {
        this(ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
    }

    private TesseraCommand(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
        this.validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();
    }

    @CommandLine.Option(
            names = {"--configfile", "-configfile"},
            description = "Path to node configuration file")
    public Config config;

    @CommandLine.Option(
            names = {"--pidfile", "-pidfile"},
            description = "Create a file at the specified path containing the process' ID (PID)")
    public Path pidFilePath;

    @CommandLine.Option(
            names = {"-o", "--override"},
        description = "Override a value in the configuration. Can be used multiple times.")
    private final Map<String, String> overrides = new LinkedHashMap<>();

    @CommandLine.Option(
            names = {"-r", "--recover"},
            description = "Start Tessera in recovery mode")
    private boolean recover;

    @CommandLine.Mixin public DebugOptions debugOptions;

    // TODO(cjh) dry run option to print effective config to terminal to allow review of CLI overrides

    @Override
    public CliResult call() throws Exception {
        // we can't use required=true in the params for @Option as this also applies the requirement to all subcmds
        if (Objects.isNull(config)) {
            throw new NoTesseraConfigfileOptionException();
        }

        overrides.forEach((target, value) -> {
            LOGGER.debug("Setting : {} with value(s) {}", target, value);
            OverrideUtil.setValue(config, target, value);
            LOGGER.debug("Set : {} with value(s) {}", target, value);
        });

        if (recover) {
            config.setRecoveryMode(true);
        }

        final Set<ConstraintViolation<Config>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        keyPasswordResolver.resolveKeyPasswords(config);

        if (Objects.nonNull(pidFilePath)) {
            // TODO(cjh) duplication with PidFileMixin.class
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

        return new CliResult(0, false, config);
    }
}
