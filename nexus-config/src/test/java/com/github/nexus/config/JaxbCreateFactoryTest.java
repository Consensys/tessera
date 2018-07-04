package com.github.nexus.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class JaxbCreateFactoryTest {

    private final Class type;

    public JaxbCreateFactoryTest(final Class type) {
        this.type = type;
    }

    @Parameterized.Parameters
    public static List<Class> params() {
        return Arrays.asList(
                ArgonOptions.class,
                Config.class,
                KeyData.class,
                Peer.class,
                ServerConfig.class,
                SslConfig.class,
                JdbcConfig.class,
                PrivateKey.class,
                PrivateKeyData.class
        );

    }

    @Test
    public void createDefault() throws Exception {

        final Method factoryMethod = type.getDeclaredMethod("create");
        factoryMethod.setAccessible(true);

        final Object instance = factoryMethod.invoke(null);

        assertThat(instance).isNotNull();

    }

}
