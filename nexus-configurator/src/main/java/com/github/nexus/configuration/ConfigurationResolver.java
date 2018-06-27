package com.github.nexus.configuration;

import com.github.nexus.configuration.interceptor.ConfigurationInterceptor;
import com.github.nexus.configuration.interceptor.FileLoadingInterceptor;

import java.util.Properties;

/**
 * Performs string manipulation to change properties with specific values into other values.
 */
public interface ConfigurationResolver {

    ConfigurationInterceptor[] INTERCEPTORS = new ConfigurationInterceptor[]{
        new FileLoadingInterceptor()
    };

    /**
     * Converts properties by passing all the values through a property resolver that
     * is registered in this expression context
     *
     * @param properties The properties object whose values are to be resolved
     * @return The resolved property map
     */
    Properties resolveProperties(Properties properties);

    static ConfigurationResolver create() {
        return new ConfigurationResolverImpl(INTERCEPTORS);
    }

}
