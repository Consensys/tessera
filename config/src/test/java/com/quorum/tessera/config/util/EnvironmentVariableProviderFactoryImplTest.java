package com.quorum.tessera.config.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentVariableProviderFactoryImplTest {

    @Test
    public void create() {
        EnvironmentVariableProviderFactoryImpl factory = new EnvironmentVariableProviderFactoryImpl();

        assertThat(factory.create()).isExactlyInstanceOf(EnvironmentVariableProvider.class);
    }
}
