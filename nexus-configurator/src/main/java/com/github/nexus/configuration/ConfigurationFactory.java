package com.github.nexus.configuration;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
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

    public static String[] cliArgsArray = new String[]{};

    private static final String CONFIG_FILE_PROPERTY = "configfile";

    private static final String[] KNOWN_PROPERTIES = new String[] {
        "publicKeys", "privateKeys", "port", "url", "othernodes"
    };

    private static Options options = new Options(){{
        addOption("publicKeys", "publicKeys", true, "public keys");
        addOption("privateKeys", "privateKeys", true, "private keys");
        addOption("configfile", "configfile", true, "config file location");
    }};

    private ConfigurationInterceptor[] interceptors = new ConfigurationInterceptor[]{
        new FileLoadingInterceptor()
    };

    public ConfigurationFactory() throws NoSuchMethodException {
    }

    /**
     * Turns CLI arguments into properties that can be used in the application.
     * The set of accepted properties is the same as ones are can be passed in
     * via a configuration file
     *
     * @param args the commandline arguments to parse
     */
    static Properties getCliProperties(final String... args) {
        final Properties properties = new Properties();

        try {
            new DefaultParser()
                .parse(options, args)
                .iterator()
                .forEachRemaining(opt -> properties.setProperty(opt.getOpt(), opt.getValue()));
        } catch (final ParseException ex) {
            throw new RuntimeException(ex);
        }

        return properties;
    }

    /**
     * Attempts to extract the "configfile" property from the arguments
     * and return a List containing the path to that file
     *
     * @param args the arguments to search
     * @return a list potentially containing a path to a configuration file
     */
    static List<Path> getConfigFilePath(final String... args) {
        return Stream
            .of(getCliProperties(args).getProperty(CONFIG_FILE_PROPERTY))
            .filter(Objects::nonNull)
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .collect(toList());
    }

    public static Properties properties(final String... args) {

        final ConfigurationSource configuration = new MergeConfigurationSource(
            new ClasspathConfigurationSource(() -> singletonList(Paths.get("application-default.properties"))),
            new FilesConfigurationSource(() -> ConfigurationFactory.getConfigFilePath(args)),
            new InMemoryConfigurationSource(getCliProperties(args)),
            new EnvironmentVariablesConfigurationSource(),
            new SystemPropertiesConfigurationSource()
        );

        configuration.init();

        final Properties filteredProperties = new Properties();
        configuration
            .getConfiguration(new ImmutableEnvironment(""))
            .entrySet()
            .stream()
            .filter((k) -> Stream.of(KNOWN_PROPERTIES).anyMatch(prop -> prop.equals(k.getKey())))
            .forEach(k -> filteredProperties.setProperty(k.getKey().toString(), k.getValue().toString()));

        return filteredProperties;
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
    public static Configuration init() throws NoSuchMethodException {

        final ConfigurationFactory factory = new ConfigurationFactory();

        final ConfigurationResolverImpl resolver = new ConfigurationResolverImpl(factory.interceptors);

        final Properties resolvedProperties = resolver.resolveProperties(properties(cliArgsArray));

        final InMemoryConfigurationSource configurationSource = new InMemoryConfigurationSource(resolvedProperties);

        return new ConfigurationProviderBuilder()
            .withConfigurationSource(configurationSource)
            .build()
            .bind("", Configuration.class);
    }

}
