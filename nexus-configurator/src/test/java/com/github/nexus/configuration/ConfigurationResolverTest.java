package com.github.nexus.configuration;

import com.github.nexus.configuration.interceptor.ConfigurationInterceptor;
import org.junit.Test;

import javax.el.ELContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConfigurationResolverTest {

    @Test
    public void interceptorRegistersWithContext() {

        final ConfigurationInterceptor interceptor = mock(ConfigurationInterceptor.class);

        new ConfigurationResolverImpl(interceptor);

        verify(interceptor).register(any(ELContext.class));
    }

}
