package com.github.nexus.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JaxbCreateFactoryTest {

    private Class type;

    public JaxbCreateFactoryTest(Class type) {
        this.type = type;
    }

    @Parameterized.Parameters
    public static List<Class> params() {
        return Arrays.asList(
                ArgonOptions.class,
                Config.class,
                KeyData.class,
                Peer.class,
                PrivateKey.class,
                PublicKey.class,
                ServerConfig.class,
                SslConfig.class,
                JdbcConfig.class);

    }

    @Test
    public void createDefault() throws Exception {

        Method factoryMethod = type.getDeclaredMethod("create");
        factoryMethod.setAccessible(true);

        Object instance = factoryMethod.invoke(null);

        assertThat(instance).isNotNull();

    }
}
