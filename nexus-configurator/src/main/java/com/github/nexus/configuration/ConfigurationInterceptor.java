package com.github.nexus.configuration;

import javax.el.ELContext;

@FunctionalInterface
public interface ConfigurationInterceptor {

    void register(ELContext eLContext);

}
