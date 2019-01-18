package com.quorum.tessera.config.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EnvironmentVariableProviderTest {

    @Test
    public void getEnv() {
        EnvironmentVariableProvider provider = new EnvironmentVariableProvider();

        //returns null as env variables not set in test environment
        assertThat(provider.getEnv("env")).isNull();
    }

    @Test
    public void getEnvAsCharArray() {
        EnvironmentVariableProvider provider = new EnvironmentVariableProvider();

        //returns null as env variables not set in test environment
        assertThat(provider.getEnvAsCharArray("env")).isNull();
    }
}
