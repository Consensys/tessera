package com.quorum.tessera.enclave.server;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.cli.*;
import com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.cli.parsers.PidFileParser;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@picocli.CommandLine.Command
public class EnclaveCliAdapter implements CliAdapter, Callable<CliResult> {

    @picocli.CommandLine.Unmatched private String[] allParameters = new String[0];

    private final CommandLineParser parser;

    private final KeyPasswordResolver keyPasswordResolver;

    public EnclaveCliAdapter(final CommandLineParser parser, final KeyPasswordResolver keyPasswordResolver) {
        this.parser = Objects.requireNonNull(parser);
        this.keyPasswordResolver = Objects.requireNonNull(keyPasswordResolver);
    }

    public EnclaveCliAdapter() {
        this(
                new DefaultParser(),
                ServiceLoaderUtil.load(KeyPasswordResolver.class).orElse(new CliKeyPasswordResolver()));
    }

    @Override
    public CliType getType() {
        return CliType.ENCLAVE;
    }

    @Override
    public CliResult call() throws Exception {
        return this.execute(allParameters);
    }

    @Override
    public CliResult execute(String... args) throws Exception {

        final Options options = new Options();

        options.addOption(
                Option.builder("configfile")
                        .desc("Path to configuration file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .build());

        options.addOption(
                Option.builder("pidfile")
                        .desc("Path to pid file")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .build());

        final List<String> argsList = Arrays.asList(args);
        if (argsList.contains("help") || argsList.isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(200);
            formatter.printHelp("enclave -configfile <PATH>", options);
            return new CliResult(0, true, null);
        }

        try {
            final CommandLine line = parser.parse(options, args);
            new PidFileParser().parse(line);
            String configfile = line.getOptionValue("configfile");

            Config config = JaxbUtil.unmarshal(Files.newInputStream(Paths.get(configfile)), Config.class);

            keyPasswordResolver.resolveKeyPasswords(config);

            return new CliResult(0, false, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }
    }

    // TODO: for testing, remove if possible
    public void setAllParameters(final String[] allParameters) {
        this.allParameters = allParameters;
    }
}
