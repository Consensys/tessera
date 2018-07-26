package com.quorum.tessera.config;

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

    private final OtherType otherType = new OtherType();

    static class OtherType {
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
                KeyDataConfig.class,
                PrivateKeyData.class
        );

    }

    @Test
    public void createDefault() throws Exception {

        final Method factoryMethod = type.getDeclaredMethod("create");
        factoryMethod.setAccessible(true);

        final Object instance = factoryMethod.invoke(null);

        assertThat(instance).isNotNull();

        assertThat(instance).isEqualTo(instance);
        assertThat(instance.hashCode()).isEqualTo(instance.hashCode());
        assertThat(instance).isNotEqualTo(otherType);
        assertThat(instance).isNotEqualTo(null);

    }

    @Test
    public void ensureThatEqualsIncludesType() throws Exception {

        final Method factoryMethod = ServerConfig.class.getDeclaredMethod("create");
        factoryMethod.setAccessible(true);

        Object firstObject = factoryMethod.invoke(null);

        final Method anotherFactoryMethod = SslConfig.class.getDeclaredMethod("create");
        anotherFactoryMethod.setAccessible(true);
        Object secondObject = anotherFactoryMethod.invoke(null);

        assertThat(firstObject).isNotEqualTo(secondObject);

    }

}
