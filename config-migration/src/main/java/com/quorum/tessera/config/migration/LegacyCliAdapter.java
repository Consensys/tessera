package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LegacyCliAdapter implements CliAdapter {

    private final FilesDelegate fileDelegate = FilesDelegate.create();

    private final TomlConfigFactory configFactory;

    public LegacyCliAdapter() {
        this.configFactory = new TomlConfigFactory();
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
            return new CliResult(exitCode, true, false, null);
        }

        CommandLineParser parser = new DefaultParser();

        CommandLine line = parser.parse(options, args);

        final ConfigBuilder configBuilder = Optional.ofNullable(line.getOptionValue("tomlfile"))
                                                .map(Paths::get)
                                                .map(fileDelegate::newInputStream)
                                                .map(stream -> this.configFactory.create(stream, null))
                                                .orElse(ConfigBuilder.create());

        final KeyDataBuilder keyDataBuilder = Optional.ofNullable(line.getOptionValue("tomlfile"))
                                                    .map(Paths::get)
                                                    .map(fileDelegate::newInputStream)
                                                    .map(configFactory::createKeyDataBuilder)
                                                    .orElse(KeyDataBuilder.create());

        ConfigBuilder adjustedConfig = applyOverrides(line, configBuilder, keyDataBuilder);

        Config config = adjustedConfig.build();

        String outputfilevalue = line.getOptionValue("outputfile", "tessera-config.json");

        Path outputPath = Paths.get(outputfilevalue).toAbsolutePath();

        return writeToOutputFile(config, outputPath);
    }

    static CliResult writeToOutputFile(Config config, Path outputPath) throws IOException {

        System.out.printf("Saving config to  %s", outputPath);
        System.out.println();
        JaxbUtil.marshalWithNoValidation(config, System.out);
        System.out.println();

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            JaxbUtil.marshal(config, outputStream);
            System.out.printf("Saved config to  %s", outputPath);
            System.out.println();
            return new CliResult(0, false, false, config);
        } catch (ConstraintViolationException validationException) {
            validationException.getConstraintViolations()
                .stream()
                .map(cv -> "Warning: " + cv.getMessage() + " on property " + cv.getPropertyPath())
                .forEach(System.err::println);

            Files.write(outputPath, JaxbUtil.marshalToStringNoValidation(config).getBytes());
            System.out.printf("Saved config to  %s", outputPath);
            System.out.println();
            return new CliResult(2, false, false, config);
        }
    }

    static ConfigBuilder applyOverrides(CommandLine line, ConfigBuilder configBuilder, KeyDataBuilder keyDataBuilder) {

        Optional.ofNullable(line.getOptionValue("workdir"))
            .ifPresent(configBuilder::workdir);

        Optional.ofNullable(line.getOptionValue("workdir"))
            .ifPresent(keyDataBuilder::withWorkingDirectory);

        Optional.ofNullable(line.getOptionValue("url"))
                .map(url -> {
                    try {
                        return new URL(url);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Bad server url given: " + e.getMessage());
                    }
                }).map(uri -> uri.getProtocol() + "://" + uri.getHost())
                .ifPresent(configBuilder::serverHostname);

        Optional.ofNullable(line.getOptionValue("port"))
                .map(Integer::valueOf)
                .ifPresent(configBuilder::serverPort);

        Optional.ofNullable(line.getOptionValue("socket"))
                .ifPresent(configBuilder::unixSocketFile);

        Optional.ofNullable(line.getOptionValues("othernodes"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::peers);

        Optional.ofNullable(line.getOptionValues("publickeys"))
                .map(Arrays::asList)
                .ifPresent(keyDataBuilder::withPublicKeys);

        Optional.ofNullable(line.getOptionValues("privatekeys"))
                .map(Arrays::asList)
                .ifPresent(keyDataBuilder::withPrivateKeys);

        Optional.ofNullable(line.getOptionValues("alwayssendto"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::alwaysSendTo);

        Optional.ofNullable(line.getOptionValue("passwords"))
                .ifPresent(keyDataBuilder::withPrivateKeyPasswordFile);

        Optional.ofNullable(line.getOptionValue("storage"))
                .map(JdbcConfigFactory::fromLegacyStorageString)
                .ifPresent(configBuilder::jdbcConfig);

        if(line.hasOption("ipwhitelist")) {
            configBuilder.useWhiteList(true);
        }

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

        Optional.ofNullable(line.getOptionValue("tlsservercert"))
                .ifPresent(configBuilder::sslServerTlsCertificatePath);

        Optional.ofNullable(line.getOptionValue("tlsclientcert"))
                .ifPresent(configBuilder::sslClientTlsCertificatePath);

        Optional.ofNullable(line.getOptionValues("tlsserverchain"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::sslServerTrustCertificates);

        Optional.ofNullable(line.getOptionValues("tlsclientchain"))
                .map(Arrays::asList)
                .ifPresent(configBuilder::sslClientTrustCertificates);

        Optional.ofNullable(line.getOptionValue("tlsserverkey"))
            .ifPresent(configBuilder::sslServerTlsKeyPath);

        Optional.ofNullable(line.getOptionValue("tlsclientkey"))
            .ifPresent(configBuilder::sslClientTlsKeyPath);

        Optional.ofNullable(line.getOptionValue("tlsknownservers"))
            .ifPresent(configBuilder::sslKnownServersFile);

        Optional.ofNullable(line.getOptionValue("tlsknownclients"))
            .ifPresent(configBuilder::sslKnownClientsFile);

        final KeyConfiguration keyConfiguration = keyDataBuilder.build();

        if (!keyConfiguration.getKeyData().isEmpty()) {
            configBuilder.keyData(keyConfiguration);
        } else {
            if(Optional.ofNullable(line.getOptionValue("passwords")).isPresent()) {
                System.err.println("Info: Public/Private key data not provided in overrides.  Overriden password file has not been added to config.");
            }
        }

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
                        .valueSeparator(',')
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
                        .valueSeparator(',')
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
                .desc("If provided, Tessera will use the othernodes as a whitelist.")
                .hasArg(false)
                .build()
        );

        return options;
    }
}
