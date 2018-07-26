package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.builder.SslTrustModeFactory;
import com.quorum.tessera.io.FilesDelegate;
import org.apache.commons.cli.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class LegacyCliAdapter implements CliAdapter {

    private final FilesDelegate fileDelegate = FilesDelegate.create();

    private final ConfigFactory configFactory;

    public LegacyCliAdapter() {
        this(new com.quorum.tessera.config.cli.TomlConfigFactory());
    }

    protected LegacyCliAdapter(ConfigFactory configFactory) {
        this.configFactory = Objects.requireNonNull(configFactory);
    }

    @Override
    public CliResult execute(String... args) throws Exception {

        Options options = buildOptions();

        CommandLineParser parser = new DefaultParser();

        CommandLine line = parser.parse(options, args);

        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            //(If a configuration file is specified, any command line options will take precedence.)
            formatter.printHelp("tessera [OPTION...] [config file containing options]", options);
            return new CliResult(0, true, null);
        }

        final Pattern configFileSearch = Pattern.compile("^--.*=.$");

        final String lastArg = line.getArgList().get(line.getArgList().size() - 1);

        final ConfigBuilder configBuilder = Optional.of(lastArg)
                .filter(configFileSearch.asPredicate().negate())
                .map(v -> Paths.get(v))
                .map(fileDelegate::newInputStream)
                .map(configData -> configFactory.create(configData))
                .map(ConfigBuilder::from)
                .orElse(ConfigBuilder.create());

        
        ConfigBuilder adjustedConfig = applyOverrides(line,configBuilder);

        return new CliResult(0, false, adjustedConfig.build());
    }

    static ConfigBuilder applyOverrides(CommandLine line, ConfigBuilder configBuilder) {

        Optional.ofNullable(line.getOptionValue("url"))
                .ifPresent(configBuilder::serverHostname);

        Optional.ofNullable(line.getOptionValue("port"))
                .map(Integer::valueOf)
                .ifPresent(configBuilder::serverPort);

        Optional.ofNullable(line.getOptionValue("socket"))
                .ifPresent(configBuilder::unixSocketFile);

        Optional.ofNullable(line.getOptionValues("othernodes"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::peers);

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create();

        Optional.ofNullable(line.getOptionValues("publickeys"))
                .map(Arrays::asList)
                .ifPresent(keyDataBuilder::withPublicKeys);

        Optional.ofNullable(line.getOptionValues("privatekeys"))
                .map(Arrays::asList)
                .ifPresent(keyDataBuilder::withPrivateKeys);

        Optional.ofNullable(line.getOptionValue("passwords"))
                .map(p -> Paths.get(p))
                .ifPresent(keyDataBuilder::withPrivateKeyPasswordFile);

        
       Optional.ofNullable(line.getOptionValue("storage"))
               .map(JdbcConfigFactory::fromLegacyStorageString)
               .ifPresent(configBuilder::jdbcConfig);
        
       Optional.ofNullable(line.getOptionValue("tlsservertrust"))
               .map(SslTrustModeFactory::resolveByLegacyValue)
               .ifPresent(configBuilder::sslServerTrustMode);
       
       
       Optional.ofNullable(line.getOptionValue("tlsclienttrust"))
               .map(SslTrustModeFactory::resolveByLegacyValue)
               .ifPresent(configBuilder::sslClientTrustMode);
       
       Optional.ofNullable(line.getOptionValue("tlsservercert"))
               .ifPresent(configBuilder::sslServerKeyStorePath);
       
       Optional.ofNullable(line.getOptionValue("tlsclientcert"))
               .ifPresent(configBuilder::sslClientKeyStorePath); 
       
       Optional.ofNullable(line.getOptionValues("tlsserverchain"))
               .map(Arrays::asList)
               .ifPresent(configBuilder::sslServerTrustCertificates);
       
       Optional.ofNullable(line.getOptionValues("tlsclientchain"))
               .map(Arrays::asList)
               .ifPresent(configBuilder::sslClientTrustCertificates);
       
        configBuilder.keyData(keyDataBuilder.build());

        return configBuilder;
    }

    static Options buildOptions() {

        Options options = new Options();
        options.addOption(
                Option.builder()
                        .longOpt("url")
                        .desc("URL for this node (what's advertised to other nodes, e.g. https://constellation.mydomain.com/)")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("URL")
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("port")
                        .desc("Port to listen on for the public API")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("NUM")
                        .valueSeparator('=')
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("workdir")
                        .desc("Working directory to use (relative paths specified for other options are relative to the working directory)")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("DIR")
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("socket")
                        .desc("Path to IPC socket file to create for private API access")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("FILE")
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("othernodes")
                        .desc("Comma-separated list of other node URLs to connect to on startup (this list may be incomplete)")
                        .hasArg(true)
                        .optionalArg(false)
                        .hasArgs()
                        .argName("URL...")
                        .valueSeparator(',')
                        .build());

        options.addOption(
                Option.builder()
                        .longOpt("publickeys")
                        .desc("Comma-separated list of paths to public keys to advertise")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("privatekeys")
                        .desc("Comma-separated list of paths to corresponding private keys (these must be given in the same order as --publickeys)")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("passwords")
                        .desc("A file containing the passwords for the specified --privatekeys, one per line, in the same order (if one key is not locked, add an empty line)")
                        .optionalArg(false)
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("alwayssendto")
                        .desc("Comma-separated list of paths to public keys that are always included as recipients (these must be advertised somewhere)")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("storage")
                        .optionalArg(false)
                        .desc("Storage string specifying a storage engine and/or storage path")
                        .hasArg()
                        .build()
        );



        options.addOption(
                Option.builder()
                        .longOpt("tls")
                        .desc("TLS status (strict, off)")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsservercert")
                        .desc("TLS certificate file to use for the public API")
                        .argName("FILE")
                        .numberOfArgs(1)
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsserverchain")
                        .desc("Comma separated list of TLS chain certificates to use for the public API")
                        .argName("FILE...")
                        .hasArgs()
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsserverkey")
                        .desc("TLS key to use for the public API")
                        .optionalArg(false)
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsservertrust")
                        .optionalArg(false)
                        .desc("TLS server trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
                        .build()
        );

        //           --tlsknownclients[=FILE]    TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownclients")
                        .desc("TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .build()
        );

        //           --tlsclientcert[=FILE]      TLS client certificate file to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientcert")
                        .desc("TLS client certificate file to use for connections to other nodes")
                        .argName("FILE")
                        .build()
        );

        //           --tlsclientchain[=FILE...]  Comma separated list of TLS chain certificates to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientchain")
                        .desc("Comma separated list of TLS chain certificates to use for connections to other nodes")
                        .argName("FILE...")
                        .hasArgs()
                        .build()
        );

        //           --tlsclientkey[=FILE]       TLS key to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientkey")
                        .desc("TLS key to use for connections to other nodes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //           --tlsclienttrust[=STRING]   TLS client trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)
        options.addOption(
                Option.builder()
                        .longOpt("tlsclienttrust")
                        .desc("TLS client trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
                        .argName("STRING")
                        .hasArg()
                        .build()
        );

//           --tlsknownservers[=FILE]    TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownservers")
                        .desc("TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //  -v[NUM]  --verbosity[=NUM]           Print more detailed information (optionally specify a number or add v's to increase verbosity)
        options.addOption(
                Option.builder("v")
                        .longOpt("verbosity")
                        .desc("Print more detailed information (optionally specify a number or add v's to increase verbosity)")
                        .argName("NUM")
                        .hasArg()
                        .build()
        );

        //  -V, -?   --version                   Output current version information, then exit
        options.addOption(
                Option.builder("V")
                        .longOpt("version")
                        .desc("Output current version information, then exit")
                        .build()
        );
        options.addOption(
                Option.builder("?")
                        .desc("Output current version information, then exit")
                        .build()
        );

//           --generatekeys[=NAME...]    Comma-separated list of key pair names to generate, then exit
        options.addOption(
                Option.builder()
                        .longOpt("generatekeys")
                        .desc("Comma-separated list of key pair names to generate, then exit")
                        .hasArgs()
                        .argName("NAME...")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("help")
                        .build()
        );

        return options;
    }
}
