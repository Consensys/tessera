package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.PidFileMixin;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.*;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "tessera",
    header = "Tessera - Privacy Manager for Quorum%n",
    descriptionHeading = "%nDescription: ",
    description = "Start a Tessera node.  Subcommands exist to manage Tessera encryption keys%n",
    parameterListHeading = "Parameters:%n",
    commandListHeading = "%nCommands:%n",
    optionListHeading = "Options:%n",
    abbreviateSynopsis = true)
public class TesseraCommand implements Callable<CliResult> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TesseraCommand.class);

  private static final String LEGACY_OVERRIDE_EXCEPTION_MSG =
      "Invalid config overrides. Consider using the --override option instead.";

  private final Validator validator;

  private final KeyPasswordResolver keyPasswordResolver;

  public TesseraCommand() {
    this(
        ServiceLoader.load(KeyPasswordResolver.class)
            .findFirst()
            .orElse(new CliKeyPasswordResolver()),
        Validation.byDefaultProvider()
            .configure()
            .ignoreXmlConfiguration()
            .buildValidatorFactory()
            .getValidator());
  }

  private TesseraCommand(final KeyPasswordResolver keyPasswordResolver, Validator validator) {
    this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    this.validator = Objects.requireNonNull(validator);
  }

  @CommandLine.Option(
      names = {"--configfile", "-configfile", "--config-file"},
      description = "Path to node configuration file")
  public Config config;

  @CommandLine.Mixin private final PidFileMixin pidFileMixin = new PidFileMixin();

  @CommandLine.Option(
      names = {"-o", "--override"},
      description = "Override a value in the configuration. Can be used multiple times.")
  private final Map<String, String> overrides = new LinkedHashMap<>();

  @CommandLine.Option(
      names = {"-r", "--recover"},
      description = "Start Tessera in recovery mode")
  private boolean recover;

  @CommandLine.Mixin public DebugOptions debugOptions;

  @CommandLine.Unmatched public List<String> unmatchedEntries;

  // TODO(cjh) dry run option to print effective config to terminal to allow review of CLI overrides

  @Override
  public CliResult call() throws Exception {
    // we can't use required=true in the params for @Option as this also applies the requirement to
    // all subcmds
    if (Objects.isNull(config)) {
      throw new NoTesseraConfigfileOptionException();
    }

    // overrides using '-o <KEY>=<VALUE>'
    overrides.forEach(this::overrideConfigValue);

    // legacy overrides using unmatched options
    if (Objects.nonNull(unmatchedEntries)) {
      LOGGER.warn(
          "Using unmatched CLI options for config overrides is deprecated.  Use the --override option instead.");

      List<String> unmatched = new ArrayList<>(unmatchedEntries);

      for (int i = 0; i < unmatched.size(); i += 2) {
        String line = unmatched.get(i);
        if (!line.startsWith("-")) {
          throw new CliException(LEGACY_OVERRIDE_EXCEPTION_MSG);
        }
        final String target = line.replaceFirst("-{1,2}", "");
        final int nextIndex = i + 1;
        if (nextIndex > (unmatched.size() - 1)) {
          throw new CliException(LEGACY_OVERRIDE_EXCEPTION_MSG);
        }
        final String value = unmatched.get(nextIndex);
        try {
          overrideConfigValue(target, value);
        } catch (CliException ex) {
          throw new CliException(String.join("\n", LEGACY_OVERRIDE_EXCEPTION_MSG, ex.getMessage()));
        }
      }
    }

    if (recover) {
      config.setRecoveryMode(true);
    }

    final Set<ConstraintViolation<Config>> violations = validator.validate(config);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    keyPasswordResolver.resolveKeyPasswords(config);

    pidFileMixin.createPidFile();

    return new CliResult(0, false, config);
  }

  private void overrideConfigValue(String target, String newValue) {
    LOGGER.debug("Setting : {} with value(s) {}", target, newValue);
    try {
      OverrideUtil.setValue(config, target, newValue);
    } catch (ReflectException ex) {
      throw new CliException(ex.getMessage());
    }
    LOGGER.debug("Set : {} with value(s) {}", target, newValue);
  }
}
