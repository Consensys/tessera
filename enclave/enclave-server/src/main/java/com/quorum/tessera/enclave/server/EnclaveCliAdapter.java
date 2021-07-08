package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.PidFileMixin;
import com.quorum.tessera.config.Config;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "enclave",
    header = "Run a standalone enclave to perform encryption/decryption operations for Tessera",
    descriptionHeading = "%nDescription: ",
    description =
        "Run a standalone enclave, which will perform encryption/decryption operations "
            + "for a transaction manager. This means that the transaction manager does not perform any of the "
            + "operations inside its own process, shielding the user from potential attacks.",
    optionListHeading = "%nOptions:%n",
    subcommands = CommandLine.HelpCommand.class)
public class EnclaveCliAdapter implements CliAdapter, Callable<CliResult> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveCliAdapter.class);

  @CommandLine.Option(
      names = {"--configfile", "-configfile"},
      description = "Path to enclave configuration file",
      required = true)
  private Config config;

  @CommandLine.Mixin private final PidFileMixin pidFileMixin = new PidFileMixin();

  private final KeyPasswordResolver keyPasswordResolver;

  public EnclaveCliAdapter(final KeyPasswordResolver keyPasswordResolver) {
    this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
  }

  public EnclaveCliAdapter() {
    this(
        ServiceLoader.load(KeyPasswordResolver.class)
            .findFirst()
            .orElse(new CliKeyPasswordResolver()));
  }

  @Override
  public CliType getType() {
    return CliType.ENCLAVE;
  }

  @Override
  public CliResult call() throws Exception {
    return this.execute();
  }

  @Override
  public CliResult execute(String... args) throws Exception {
    this.pidFileMixin.createPidFile();

    // to make it this far, the configuration has to be set and valid

    keyPasswordResolver.resolveKeyPasswords(config);

    return new CliResult(0, false, config);
  }
}
