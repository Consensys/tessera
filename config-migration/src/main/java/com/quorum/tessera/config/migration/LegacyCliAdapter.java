package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.builder.SslTrustModeFactory;
import com.quorum.tessera.config.cli.CliAdapter;
import com.quorum.tessera.config.cli.CliResult;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import org.apache.commons.cli.*;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LegacyCliAdapter implements CliAdapter {

    private final FilesDelegate fileDelegate = FilesDelegate.create();

    private final ConfigFactory configFactory;

    public LegacyCliAdapter() {
        this(new TomlConfigFactory());
    }

    protected LegacyCliAdapter(ConfigFactory configFactory) {
        this.configFactory = Objects.requireNonNull(configFactory);
    }

    @Override
    public CliResult execute(String... args) throws Exception {

        Options options = buildOptions();
        final List<String> argsList = Arrays.asList(args);
        if (argsList.isEmpty() || argsList.contains("help")) {
            String header = "Generate Tessera JSON config file from a Constellation TOML config file";
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("tessera-config-migration", header, options, null);
            final int exitCode = argsList.isEmpty() ? 1 : 0;
            return new CliResult(exitCode, true, null);
        }

        CommandLineParser parser = new DefaultParser();

        CommandLine line = parser.parse(options, args);

        final ConfigBuilder configBuilder = Optional.ofNullable(line.getOptionValue("tomlfile"))
                                                .map(v -> Paths.get(v))
                                                .map(fileDelegate::newInputStream)
                                                .map(configData -> configFactory.create(configData))
                                                .map(ConfigBuilder::from)
                                                .orElse(ConfigBuilder.create());

        ConfigBuilder adjustedConfig = applyOverrides(line, configBuilder);

        Config config = adjustedConfig.build();

        String outputfilevalue = line.getOptionValue("outputfile", "tessera-config.json");

        Path outputPath = Paths.get(outputfilevalue).toAbsolutePath();

        return writeToOutputFile(config, outputPath);
    }

    static CliResult writeToOutputFile(Config config, Path outputPath) throws IOException {

        System.out.printf("Saving config to  %s", outputPath);
        System.out.println();
        System.out.println(JaxbUtil.marshalToStringNoValidation(config));
        System.out.println();

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            JaxbUtil.marshal(config, outputStream);
            System.out.printf("Saved config to  %s", outputPath);
            System.out.println();
            return new CliResult(0, false, config);
        } catch (ConstraintViolationException validationException) {
            validationException.getConstraintViolations().stream()
                    .forEach(System.err::println);
            return new CliResult(2, false, config);
        }
    }

    static Optional<Path> resolveUnixFilePath(Path initial, String workdir, String fileName) {
        if (Objects.nonNull(workdir) && Objects.nonNull(fileName)) {
            return Optional.of(Paths.get(workdir, fileName));
        } else if(Objects.nonNull(fileName)) {
            return Optional.of(Paths.get(fileName));
        }

        return Optional.ofNullable(initial);

    }

    static Optional<List<Path>> resolveListOfUnixFilePaths(List<Path> initial, String workdir, List<String> fileNames) {
        if(Objects.nonNull(workdir) && Objects.nonNull(fileNames)) {
            List<Path> paths = new ArrayList<>();
            for(String fileName : fileNames) {
                paths.add(Paths.get(workdir, fileName));
            }
            return Optional.of(paths);
        }

        return Optional.ofNullable(initial);
    }

    static ConfigBuilder applyOverrides(CommandLine line, ConfigBuilder configBuilder) {

        Config initialConfig = configBuilder.build();

        Optional.ofNullable(line.getOptionValue("url"))
                .ifPresent(configBuilder::serverHostname);

        Optional.ofNullable(line.getOptionValue("port"))
                .map(Integer::valueOf)
                .ifPresent(configBuilder::serverPort);

        resolveUnixFilePath(initialConfig.getUnixSocketFile(), line.getOptionValue("workdir"), line.getOptionValue("socket"))
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

        Optional.ofNullable(line.getOptionValues("alwayssendto"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::alwaysSendTo);

        Optional.ofNullable(line.getOptionValue("workdir"))
                .ifPresent(keyDataBuilder::withWorkingDirectory);

        resolveUnixFilePath(initialConfig.getKeys().getPasswordFile(), line.getOptionValue("workdir"), line.getOptionValue("passwords"))
            .ifPresent(keyDataBuilder::withPrivateKeyPasswordFile);

        Optional.ofNullable(line.getOptionValue("storage"))
                .map(JdbcConfigFactory::fromLegacyStorageString)
                .ifPresent(configBuilder::jdbcConfig);

        Optional.ofNullable(line.getOptionValue("ipwhitelist"))
                .ifPresent(v -> configBuilder.useWhiteList(true));

        Optional.ofNullable(line.getOptionValue("tls"))
                .map(String::toUpperCase)
                .map(SslAuthenticationMode::valueOf)
                .ifPresent(configBuilder::sslAuthenticationMode);

        Optional.ofNullable(line.getOptionValue("tlsservertrust"))
                .map(SslTrustModeFactory::resolveByLegacyValue)
                .ifPresent(configBuilder::sslServerTrustMode);

        Optional.ofNullable(line.getOptionValue("tlsclienttrust"))
                .map(SslTrustModeFactory::resolveByLegacyValue)
                .ifPresent(configBuilder::sslClientTrustMode);

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getServerTlsCertificatePath(), line.getOptionValue("workdir"), line.getOptionValue("tlsservercert"))
            .ifPresent(configBuilder::sslServerTlsCertificatePath);

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getClientTlsCertificatePath(), line.getOptionValue("workdir"), line.getOptionValue("tlsclientcert"))
            .ifPresent(configBuilder::sslClientTlsCertificatePath);

        Optional.ofNullable(line.getOptionValues("tlsserverchain"))
                .map(Arrays::asList)
                .ifPresent(l -> {
                    List<Path> sslServerTrustCertificates = resolveListOfUnixFilePaths(
                        initialConfig.getServerConfig().getSslConfig().getServerTrustCertificates(),
                        line.getOptionValue("workdir"),
                        l
                    ).get();
                    configBuilder.sslServerTrustCertificates(sslServerTrustCertificates);
                });

        Optional.ofNullable(line.getOptionValues("tlsclientchain"))
                .map(Arrays::asList)
                .ifPresent(l -> {
                    List<Path> sslClientTrustCertificates = resolveListOfUnixFilePaths(
                        initialConfig.getServerConfig().getSslConfig().getClientTrustCertificates(),
                        line.getOptionValue("workdir"),
                        l
                    ).get();
                    configBuilder.sslClientTrustCertificates(sslClientTrustCertificates);
                });

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getServerTlsKeyPath(), line.getOptionValue("workdir"), line.getOptionValue("tlsserverkey"))
            .ifPresent(configBuilder::sslServerTlsKeyPath);

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getClientTlsKeyPath(), line.getOptionValue("workdir"), line.getOptionValue("tlsclientkey"))
            .ifPresent(configBuilder::sslClientTlsKeyPath);

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getKnownServersFile(), line.getOptionValue("workdir"), line.getOptionValue("tlsknownservers"))
            .ifPresent(configBuilder::sslKnownServersFile);

        resolveUnixFilePath(initialConfig.getServerConfig().getSslConfig().getKnownClientsFile(), line.getOptionValue("workdir"), line.getOptionValue("tlsknownclients"))
            .ifPresent(configBuilder::sslKnownClientsFile);

        Optional.ofNullable(keyDataBuilder.build())
                .ifPresent(configBuilder::keyData);

        return configBuilder;
    }

    static Options buildOptions() {

        Options options = new Options();
        options.addOption(
                Option.builder()
                        .longOpt("url")
                        .desc("URL for this node (i.e. the address advertised to other nodes)")
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
                        .desc("Comma-separated list of paths to private keys (these must be given in the same corresponding order as --publickeys)")
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
                        .argName("STRING")
                        .desc("Storage string specifying a storage engine and/or storage path")
                        .hasArg()
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tls")
                        .desc("TLS status (strict, off)")
                        .optionalArg(false)
                        .argName("STATUS")
                        .hasArg()
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
                        .desc("Comma separated list of TLS chain certificate files to use for the public API")
                        .argName("FILE...")
                        .hasArgs()
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsserverkey")
                        .desc("TLS key file to use for the public API")
                        .optionalArg(false)
                        .hasArg()
                        .argName("FILE")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("tlsservertrust")
                        .optionalArg(false)
                        .hasArg()
                        .argName("STRING")
                        .desc("TLS server trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
                        .build()
        );

        //           --tlsknownclients[=FILE]    TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownclients")
                        .desc("TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //           --tlsclientcert[=FILE]      TLS client certificate file to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientcert")
                        .desc("TLS client certificate file to use for connections to other nodes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //           --tlsclientchain[=FILE...]  Comma separated list of TLS chain certificates to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientchain")
                        .desc("Comma separated list of TLS chain certificate files to use for connections to other nodes")
                        .argName("FILE...")
                        .hasArgs()
                        .build()
        );

        //           --tlsclientkey[=FILE]       TLS key to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientkey")
                        .desc("TLS key file to use for connections to other nodes")
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

        //
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownservers")
                        .desc("TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        options.addOption(
            Option.builder()
                .longOpt("tomlfile")
                .desc("TOML configuration input file.")
                .argName("FILE")
                .hasArg()
                .build()
        );

        options.addOption(
            Option.builder()
                .longOpt("outputfile")
                .desc("Location to save generated Tessera configuration file.")
                .argName("FILE")
                .hasArg()
                .build()
        );

        options.addOption(
            Option.builder()
                .longOpt("ipwhitelist")
                .desc("If provided, Tessera will use the othernodes as a whitelist.  Make sure any addresses included here are also in othernodes.")
                .hasArg()
                .argName("STRING...")
                .build()
        );

        return options;
    }
}
