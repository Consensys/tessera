package com.github.nexus.configuration;

import java.util.List;
import java.util.Properties;

public class ConfigurationParserImpl implements ConfigurationParser {

    @Override
    public Configuration config(final PropertyLoader propertyLoader, final List<String> cliParameters) {
        final Properties mergedProperties = propertyLoader.getAllProperties(cliParameters.toArray(new String[0]));

        final Properties resolvedProperties = ConfigurationResolver.create().resolveProperties(mergedProperties);

        return new ConfigurationImpl(resolvedProperties);
    }

}
