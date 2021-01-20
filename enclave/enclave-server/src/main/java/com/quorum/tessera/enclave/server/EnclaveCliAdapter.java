package com.quorum.tessera.enclave.server;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.ConfigurationMixin;
import com.quorum.tessera.cli.parsers.PidFileMixin;
import com.quorum.tessera.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "enclave",
        header = "Run a standalone enclave to perform encryption/decryption operations for Tessera",
        descriptionHeading = "%nDescription: ",
        description = "Run a standalone enclave, which will perform encryption/decryption operations " +
            "for a transaction manager. This means that the transaction manager does not perform any of the " +
            "operations inside its own process, shielding the user from potential attacks.",
        optionListHeading = "%nOptions:%n",
        abbreviateSynopsis = true
)
public class EnclaveCliAdapter implements CliAdapter, Callable<CliResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveCliAdapter.class);

    @CommandLine.Option(names = "help", usageHelp = true, description = "display this help message")
    private boolean isHelpRequested;

    @CommandLine.Mixin private ConfigurationMixin configurationMixin = new ConfigurationMixin();

    @CommandLine.Mixin private PidFileMixin pidFileMixin = new PidFileMixin();

    private final KeyPasswordResolver keyPasswordResolver;

    public EnclaveCliAdapter(final KeyPasswordResolver keyPasswordResolver) {
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public EnclaveCliAdapter() {
        this(ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
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
        // set the PID if it exists
        this.pidFileMixin.call();

        // to make it this far, the configuration has to be set and valid
        final Config config = configurationMixin.getConfig();

        keyPasswordResolver.resolveKeyPasswords(config);

        return new CliResult(0, false, config);
    }
}
