package com.github.tessera.config;

import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

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

        final Object otherInstance = factoryMethod.invoke(null);

        assertThat(instance).isNotNull();

        assertThat(instance).isEqualTo(instance);
        assertThat(instance.hashCode()).isEqualTo(instance.hashCode());
        assertThat(instance).isNotEqualTo(otherType);
        assertThat(instance).isNotEqualTo(null);
        assertThat(instance).isEqualTo(otherInstance);

        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            Object o1 = factoryMethod.invoke(null);
            Object o2 = factoryMethod.invoke(null);

            Object value = generateValueForType(field.getType());
            assertThat(value).isNotNull();

            field.set(o1, value);

            assertThat(o1)
                    .isNotEqualTo(o2);

        }
    }

    static <T> T generateValueForType(Class<T> type) throws Exception {

        if (type == String.class) {
            return (T) UUID.randomUUID().toString();
        }

        if (type == Integer.class) {
            return (T) (Integer) new Random().nextInt();

        }

        if (type == Long.class) {
            return (T) (Long) new Random().nextLong();
        }

        if (type == Path.class) {
            return (T) Paths.get(UUID.randomUUID().toString());
        }

        if (type == Boolean.class || type == boolean.class) {
            return (T) (Boolean) true;
        }

        if (params().contains(type)) {
            final Method factoryMethod = type.getDeclaredMethod("create");
            factoryMethod.setAccessible(true);
            Object o = factoryMethod.invoke(null);
            populateFieldsWithRandomStuff(o, type);
            return (T) o;

        }

        if (type.isEnum()) {
            Random random = new Random();
            return (T) Stream.of(type.asSubclass(Enum.class).getEnumConstants())
                    .sorted(Comparator.comparingInt(i -> random.nextInt()))
                    .findAny().get();
        }

        if (type == List.class) {
            return (T) Collections.emptyList();
        }

        if (type.isArray()) {
            return (T) new Object[0];
        }

        throw new UnsupportedOperationException(type.getTypeName() + " is not supported.");
    }

    static void populateFieldsWithRandomStuff(Object obj, Class type) throws Exception {

        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            field.set(obj, generateValueForType(field.getType()));

        }

    }
}
