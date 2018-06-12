package com.github.nexus.configuration;

import java.util.Properties;

/**
 * Performs string manipulation to change properties with specific values into other values.
 */
public interface ConfigurationResolver {

    /**
     * Converts properties by passing all the values through a property resolver that
     * is registered in this expression context
     *
     * @param properties The properties object whose values are to be resolved
     * @return The resolved property map
     */
    Properties resolveProperties(Properties properties);

}
