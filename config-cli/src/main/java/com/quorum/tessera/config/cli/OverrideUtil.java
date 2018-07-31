package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;

public interface OverrideUtil {

    static Map<String, Class> buildConfigOptions() {
        return fields(null, Config.class);
    }

    static String resolveName(Field field) {
        if (!field.isAnnotationPresent(XmlElement.class)) {
            return field.getName();
        }
        final XmlElement xmlElement = field.getAnnotation(XmlElement.class);
        String name = xmlElement.name();
        return Objects.equals("##default", name) ? field.getName() : name;
    }

    static Class resolveCollectionParameter(Type type) {

        return Stream.of(type)
                .map(ParameterizedType.class::cast)
                .map(pt -> pt.getActualTypeArguments()[0])
                .map(Type::getTypeName)
                .map(n -> ReflectCallback.execute(() -> Class.forName(n))).findAny().get();

    }

    static boolean isSimple(Field field) {
        return isSimple(field.getType());
    }

    static boolean isSimple(Class type) {

        if (type.getPackage() == null) {
            return true;
        }

        if (type.equals(String.class)) {
            return true;
        }

        if (type.equals(Path.class)) {
            return true;
        }

        if (type.equals(Integer.class)) {
            return true;
        }

        if (type.isEnum()) {
            return true;
        }

        return false;
    }

    static Map<String, Class> fields(String prefix, Class type) {

        String amendedPrefix = Optional.ofNullable(prefix)
                .map(s -> s.concat("."))
                .orElse("");

        Map<String, Class> list = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {

            if (isSimple(field)) {

                list.put(amendedPrefix + resolveName(field), field.getType());
                continue;
            }

            if (field.getType().getPackage().equals(Config.class.getPackage())) {
                list.putAll(fields(amendedPrefix + resolveName(field), field.getType()));
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                Class t = resolveCollectionParameter(field.getGenericType());

                if (isSimple(t)) {
                    if (t.equals(String.class)) {
                        list.put(amendedPrefix + resolveName(field), String[].class);
                    } else if(t.equals(Path.class)) {
                        list.put(amendedPrefix + resolveName(field), Path[].class);
                    } 
                    
                } else {
                    list.putAll(fields(amendedPrefix + resolveName(field), t));
                }
            }
        }

        return list;

    }

    /*
        Traverse though property path. 
        Create any nested objects and populate with provided value
     */
    static void overrideExistingValue(Config config, String propertyPath, String... value) {

        final String[] pathTokens = propertyPath.split("\\.");

        Object obj = config;
        for (int i = 0; i < pathTokens.length; i++) {
            String fieldName = pathTokens[i];
            obj = checkFieldIsInitialed(obj, fieldName, value).orElse(null);
        }
    }

    /**
     *
     * @param <T>
     * @param obj
     * @param fieldName
     * @param value
     * @return object with fieldName initialised and populated if the target
     * property
     */
    static <T> Optional<T> checkFieldIsInitialed(T obj, String fieldName, String[] value) {

        ReflectCallback<Optional<T>> reflectCallback = () -> {
            Class type = obj.getClass();
            List<Field> fields = Arrays.asList(type.getDeclaredFields());

            Field field = fields
                    .stream()
                    .filter(f -> f.isAnnotationPresent(XmlElement.class))
                    .filter(f -> f.getAnnotation(XmlElement.class).name().equals(fieldName))
                    .findAny()
                    .orElseGet(() -> ReflectCallback.execute(() -> type.getDeclaredField(fieldName)));

            field.setAccessible(true);

            final Class fieldType = field.getType();

            if (isSimple(fieldType)) {
                if (fieldType.equals(String.class)) {
                    field.set(obj, value[0]);
                }
                return Optional.of(obj);
            }

            if (Collection.class.isAssignableFrom(fieldType)) {
                Class collectionParamType = resolveCollectionParameter(field.getGenericType());
                if (isSimple(collectionParamType)) {

                    if (String.class.equals(collectionParamType)) {
                        field.set(obj, Arrays.asList(value));
                    }

                }

            }

            if (Objects.isNull(field.get(obj))) {
                Method createMethod = field.getType().getDeclaredMethod("create");
                createMethod.setAccessible(true);
                Object nested = createMethod.invoke(null);
                field.set(obj, nested);
                return Optional.of((T) nested);
            }
            return Optional.empty();
        };
        return ReflectCallback.execute(reflectCallback);
    }

}
