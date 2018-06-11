package com.github.nexus.configuration;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.inmemory.InMemoryConfigurationSource;
import org.cfg4j.source.system.EnvironmentVariablesConfigurationSource;
import org.cfg4j.source.system.SystemPropertiesConfigurationSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ConfigurationFactory {

    public ConfigurationFactory() {
        throw new UnsupportedOperationException();
    }

    private static Properties cliProperties = new Properties();

    private static final String CONFIG_FILE_PROPERTY = "config.file";

    /**
     * Turns CLI arguments into properties that can be used in the application.
     * The set of accepted properties is the same as ones are can be passed in
     * via a configuration file
     *
     * @param args the commandline arguments to parse
     */
    public static void cliParameters(final String... args) {
        final Options options = new Options();
        options.addOption("publicKeys", "publicKeys", true, "public keys");
        options.addOption("privateKeys", "privateKeys", true, "private keys");

        cliProperties.clear();

        try {
            new DefaultParser()
                .parse(options, args)
                .iterator()
                .forEachRemaining(opt -> cliProperties.setProperty(opt.getOpt(), opt.getValue()));
        } catch (final ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns a configuration class that populates configuration
     * from various sources. The property returned is based on the
     * first source that contains that property from the following list:
     *
     * system properties (passed in via -D...=...)
     * environment properties (from the OS)
     * commandline properties
     * configuration file properties (file location specified by -Dconfig.location=...)
     * default properties
     *
     * @return the configuration with the properties set
     */
    public static Configuration init() {

        final List<Path> configFile = Stream
            .of(System.getProperty(CONFIG_FILE_PROPERTY))
            .filter(Objects::nonNull)
            .map(Paths::get)
            .collect(toList());

        final MergeConfigurationSource fbcs = new MergeConfigurationSource(
            new ClasspathConfigurationSource(() -> singletonList(Paths.get("application-default.properties"))),
            new FilesConfigurationSource(() -> configFile),
            new InMemoryConfigurationSource(cliProperties),
            new EnvironmentVariablesConfigurationSource(),
            new SystemPropertiesConfigurationSource()
        );

        final ConfigurationProvider configProvider = new ConfigurationProviderBuilder()
            .withConfigurationSource(fbcs)
            .build();

        return configProvider.bind("", Configuration.class);
    }

}
