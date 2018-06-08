package com.github.nexus.config;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static com.github.nexus.config.ConfigHolder.INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigHolderTest {

    @Before
    public void init() throws Exception {

        final Field field = ConfigHolder.class.getDeclaredField("initialised");
        field.setAccessible(true);
        field.set(INSTANCE, false);

    }

    @Test
    public void getInstanceReturnsSingleton() {

        assertThat(ConfigHolder.getInstance()).isEqualTo(INSTANCE);

    }

    @Test
    public void canOnlySetConfigurationOnce(){

        final Configuration config = new Configuration();

        INSTANCE.setConfiguration(config);

        final Throwable throwable = catchThrowable(() -> INSTANCE.setConfiguration(config));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Configuration already set");

    }

    @Test
    public void configCannotBeNull() {

        final Throwable throwable = catchThrowable(() -> INSTANCE.setConfiguration(null));

        assertThat(throwable)
            .isInstanceOf(NullPointerException.class);

    }

    @Test
    public void getConfigReturnsSetConfig() {

        final Configuration config = new Configuration();

        INSTANCE.setConfiguration(config);

        assertThat(INSTANCE.getConfig()).isEqualTo(config);

    }

}
