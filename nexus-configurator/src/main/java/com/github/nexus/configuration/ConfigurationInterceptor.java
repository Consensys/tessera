package com.github.nexus.configuration;

import javax.el.ELContext;

/**
 * An interceptor provides a method reference to an ELContext that
 * it can use to change values on configuration properties at the time
 * of loading
 */
@FunctionalInterface
public interface ConfigurationInterceptor {

    /**
     * Register intercepting functions on the provided context
     * {@see FileLoadingInterceptor} for an example on how it works
     *
     * @param eLContext The context on which to provide mapping functions
     */
    void register(ELContext eLContext);

}
