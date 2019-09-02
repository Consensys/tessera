package com.quorum.tessera.config.migration;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.SystemAdapter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Generate Tessera JSON config file from a Constellation TOML config file")
public class LegacyCliAdapter implements CliAdapter, Callable<CliResult> {

    private final FilesDelegate fileDelegate;

    private final TomlConfigFactory configFactory;

    @Option(names = "help", usageHelp = true, description = "display this help message")
    private boolean isHelpRequested;

    @Option(names = "--outputfile", arity = "1", description = "the path to write the configuration to")
    private Path outputPath = Paths.get("tessera-config.json");

    @Option(names = "--tomlfile", arity = "1", description = "the path to the existing TOML configuration")
    private Path tomlfile;

    @Mixin private LegacyOverridesMixin overrides = new LegacyOverridesMixin();

    public LegacyCliAdapter() {
        this.configFactory = new TomlConfigFactory();
        this.fileDelegate = FilesDelegate.create();
    }

    @Override
    public CliType getType() {
        return CliType.CONFIG_MIGRATION;
    }

    @Override
    public CliResult call() throws Exception {
        return this.execute();
    }

    @Override
    public CliResult execute(String... args) throws Exception {

        final ConfigBuilder configBuilder =
                Optional.ofNullable(tomlfile)
                        .map(fileDelegate::newInputStream)
                        .map(stream -> this.configFactory.create(stream, null))
                        .orElse(ConfigBuilder.create());

        final KeyDataBuilder keyDataBuilder =
                Optional.ofNullable(tomlfile)
                        .map(fileDelegate::newInputStream)
                        .map(configFactory::createKeyDataBuilder)
                        .orElse(KeyDataBuilder.create());

        ConfigBuilder adjustedConfig = applyOverrides(configBuilder, keyDataBuilder);

        Config config = adjustedConfig.build();

        return writeToOutputFile(config, outputPath);
    }

    static CliResult writeToOutputFile(Config config, Path outputPath) throws IOException {
        SystemAdapter systemAdapter = SystemAdapter.INSTANCE;
        systemAdapter.out().printf("Saving config to %s", outputPath);
        systemAdapter.out().println();
        JaxbUtil.marshalWithNoValidation(config, systemAdapter.out());
        systemAdapter.out().println();

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            JaxbUtil.marshal(config, outputStream);
            systemAdapter.out().printf("Saved config to  %s", outputPath);
            systemAdapter.out().println();
            return new CliResult(0, false, config);
        } catch (ConstraintViolationException validationException) {
            validationException.getConstraintViolations().stream()
                    .map(cv -> "Warning: " + cv.getMessage() + " on property " + cv.getPropertyPath())
                    .forEach(systemAdapter.err()::println);

            Files.write(outputPath, JaxbUtil.marshalToStringNoValidation(config).getBytes());
            systemAdapter.out().printf("Saved config to  %s", outputPath);
            systemAdapter.out().println();
            return new CliResult(2, false, config);
        }
    }

    ConfigBuilder applyOverrides(ConfigBuilder configBuilder, KeyDataBuilder keyDataBuilder) {

        Optional.ofNullable(overrides.workdir).ifPresent(configBuilder::workdir);
        Optional.ofNullable(overrides.workdir).ifPresent(keyDataBuilder::withWorkingDirectory);

        Optional.ofNullable(overrides.url)
                .map(
                        url -> {
                            try {
                                return new URL(url);
                            } catch (MalformedURLException e) {
                                throw new RuntimeException("Bad server url given: " + e.getMessage());
                            }
                        })
                .map(uri -> uri.getProtocol() + "://" + uri.getHost())
                .ifPresent(configBuilder::serverHostname);

        Optional.ofNullable(overrides.port).ifPresent(configBuilder::serverPort);
        Optional.ofNullable(overrides.socket).ifPresent(configBuilder::unixSocketFile);
        Optional.ofNullable(overrides.othernodes).ifPresent(configBuilder::peers);
        Optional.ofNullable(overrides.publickeys).ifPresent(keyDataBuilder::withPublicKeys);
        Optional.ofNullable(overrides.privatekeys).ifPresent(keyDataBuilder::withPrivateKeys);
        Optional.ofNullable(overrides.alwayssendto).ifPresent(configBuilder::alwaysSendTo);
        Optional.ofNullable(overrides.passwords).ifPresent(keyDataBuilder::withPrivateKeyPasswordFile);

        Optional.ofNullable(overrides.storage)
                .map(JdbcConfigFactory::fromLegacyStorageString)
                .ifPresent(configBuilder::jdbcConfig);

        if (overrides.whitelist) {
            configBuilder.useWhiteList(true);
        }

        Optional.ofNullable(overrides.tls).ifPresent(configBuilder::sslAuthenticationMode);
        Optional.ofNullable(overrides.tlsservertrust).ifPresent(configBuilder::sslServerTrustMode);
        Optional.ofNullable(overrides.tlsclienttrust).ifPresent(configBuilder::sslClientTrustMode);
        Optional.ofNullable(overrides.tlsservercert).ifPresent(configBuilder::sslServerTlsCertificatePath);
        Optional.ofNullable(overrides.tlsclientcert).ifPresent(configBuilder::sslClientTlsCertificatePath);
        Optional.ofNullable(overrides.tlsserverchain).ifPresent(configBuilder::sslServerTrustCertificates);
        Optional.ofNullable(overrides.tlsclientchain).ifPresent(configBuilder::sslClientTrustCertificates);
        Optional.ofNullable(overrides.tlsserverkey).ifPresent(configBuilder::sslServerTlsKeyPath);
        Optional.ofNullable(overrides.tlsclientkey).ifPresent(configBuilder::sslClientTlsKeyPath);
        Optional.ofNullable(overrides.tlsknownservers).ifPresent(configBuilder::sslKnownServersFile);
        Optional.ofNullable(overrides.tlsknownclients).ifPresent(configBuilder::sslKnownClientsFile);

        final KeyConfiguration keyConfiguration = keyDataBuilder.build();

        if (!keyConfiguration.getKeyData().isEmpty()) {
            configBuilder.keyData(keyConfiguration);
        } else if (overrides.passwords != null) {
            SystemAdapter.INSTANCE
                    .err()
                    .println(
                            "Info: Public/Private key data not provided in overrides. Overriden password file has not been added to config.");
        }

        return configBuilder;
    }

    // TODO: remove. Here for testing
    public void setOverrides(final LegacyOverridesMixin overrides) {
        this.overrides = overrides;
    }
}
