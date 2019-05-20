package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.PidFileParser;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EnclaveCliAdapter implements CliAdapter {

    private final CommandLineParser parser;

    public EnclaveCliAdapter(CommandLineParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    public EnclaveCliAdapter() {
        this(new DefaultParser());
    }

    @Override
    public CliType getType() {
        return CliType.ENCLAVE;
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

        try{
            final CommandLine line = parser.parse(options, args);
            new PidFileParser().parse(line);
            String configfile = line.getOptionValue("configfile");

            Config config = JaxbUtil.unmarshal(Files.newInputStream(Paths.get(configfile)), Config.class);

            return new CliResult(0, false, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }
    }

}
