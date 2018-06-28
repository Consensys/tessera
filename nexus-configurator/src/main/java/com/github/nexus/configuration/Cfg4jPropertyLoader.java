package com.github.nexus.configuration;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
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

import static com.github.nexus.configuration.ConfigurationParser.CONFIG_FILE_PROPERTY;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Overall orchestrator for configuration loading
 * <p>
 * Contains which keys are known properties Parses provided CLI parameters and
 * merges different sources of properties to provide a single interface which
 * applications can use to fetch properties from
 */
public class Cfg4jPropertyLoader implements PropertyLoader {

    @Override
    public List<Path> getConfigFilePath(final String... args) {
        return Stream
            .of(getCliProperties(args).getProperty(CONFIG_FILE_PROPERTY))
            .filter(Objects::nonNull)
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .collect(toList());
    }

    @Override
    public Properties getCliProperties(final String... args) {
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

    @Override
    public Properties getAllProperties(final String... args) {

        final ConfigurationSource configuration = new MergeConfigurationSource(
            new ClasspathConfigurationSource(() -> singletonList(Paths.get("application-default.properties"))),
            new FilesConfigurationSource(() -> this.getConfigFilePath(args)),
            new InMemoryConfigurationSource(this.getCliProperties(args)),
            new EnvironmentVariablesConfigurationSource(),
            new SystemPropertiesConfigurationSource()
        );

        configuration.init();

        final Properties filteredProperties = new Properties();
        configuration
            .getConfiguration(new ImmutableEnvironment(""))
            .entrySet()
            .stream()
            .filter(k -> Stream.of(ConfigurationParser.KNOWN_PROPERTIES).anyMatch(k.getKey()::equals))
            .forEach(k -> filteredProperties.setProperty(k.getKey().toString(), k.getValue().toString()));

        return filteredProperties;
    }

}
