package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OverrideUtil {

    Logger LOGGER = LoggerFactory.getLogger(OverrideUtil.class);

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

    List<Class> SIMPLE_TYPES = Arrays.asList(String.class,
            Path.class, Integer.class, Boolean.class, Long.class);

    static boolean isSimple(Class type) {

        if (type.isPrimitive()) {
            return true;
        }

        if (type.getPackage() == null) {
            return true;
        }

        if (SIMPLE_TYPES.contains(type)) {
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
                    final Class arrayType = toArrayType(t);
                    list.put(amendedPrefix + resolveName(field), arrayType);

                } else {
                    list.putAll(fields(amendedPrefix + resolveName(field), t));
                }
            }
        }

        return list;

    }

    static <T> Class<T[]> toArrayType(Class<T> t) {
        return (Class<T[]>) Array.newInstance(t, 0).getClass();
    }

    /*
        Traverse though property path. 
        Create any nested objects and populate with provided value
     */
    static void overrideExistingValue(Config config, String propertyPath, String... value) {

        final String[] pathTokens = propertyPath.split("\\.");

        Object obj = config;
        for (String fieldName : pathTokens) {
            obj = setProperty(obj, fieldName, Arrays.asList(value)).orElse(null);
        }
    }

    static Field resolveField(Class type, String name) {
        return Stream.of(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(XmlElement.class))
                .filter(f -> f.getAnnotation(XmlElement.class).name().equals(name))
                .findAny()
                .orElseGet(() -> ReflectCallback.execute(() -> type.getDeclaredField(name)));
    }

    static Optional<Object> setProperty(Object obj, String fieldName, List<String> values) {

        ReflectCallback<Optional<Object>> reflectCallback = () -> {
            final Class type = obj.getClass();

            LOGGER.debug("Looking up field name {} in object: {}", fieldName, type);

            final Field field = resolveField(type, fieldName);
            field.setAccessible(true);

            LOGGER.debug("Found field name {} from {} in object: {}", field.getName(), fieldName, type);

            final Class fieldType = field.getType();

            LOGGER.debug("Resolved field name: {}#{}, type: {}", type, field.getName(), fieldType.getName());

            if (isSimple(fieldType)) {
                final String value = values.get(0);
                if (String.class.isAssignableFrom(fieldType)) {
                    field.set(obj, value);
                }
                if (Integer.class.isAssignableFrom(fieldType)) {
                    field.set(obj, Integer.valueOf(value));
                }
                if (Long.class.isAssignableFrom(fieldType)) {
                    field.set(obj, Long.valueOf(value));
                }
                if (boolean.class.isAssignableFrom(fieldType)) {
                    field.set(obj, Boolean.valueOf(value));
                }

                if (Path.class.isAssignableFrom(fieldType)) {
                    field.set(obj, Paths.get(value));
                }

                if (fieldType.isEnum()) {
                    field.set(obj, Enum.valueOf(fieldType, value));
                }

                return Optional.of(obj);
            }

            boolean isNestedConfigObject
                    = fieldType.getPackage().getName().startsWith("com.quorum.tessera");

            if (Collection.class.isAssignableFrom(fieldType)) {
                final Class genericType = resolveCollectionParameter(field.getGenericType());
                if (isSimple(genericType)) {
                    if (String.class.equals(genericType)) {
                        field.set(obj, values);
                    }
                    if (Path.class.equals(genericType)) {
                        List<Path> paths = values.stream()
                                .map(s -> Paths.get(s))
                                .collect(Collectors.toList());

                        field.set(obj, paths);
                    }
                } else {

                    
                    //TODO: Handle collectiosn of nested objects
                    Object nestedObject = createInstance(genericType);
                    List<Object> nestedObjects = Arrays.asList(nestedObject);
                    field.set(obj, nestedObject);
                    //return 
  
                }
                return Optional.of(obj);
            }

            Object existingValue = field.get(obj);
            boolean isInitialised = Objects.nonNull(existingValue) || isSimple(fieldType);

            if (isInitialised && isNestedConfigObject) {
                LOGGER.debug("Value {}#{} already initialised", type, field.getName());
                return Optional.of(existingValue);
            }

//            if (isInitialised) {
//                LOGGER.debug("Field {}#{} already populated. ", type, field.getName());
//                return Optional.of(obj);
//            }
//
//            LOGGER.debug("{} isisNestedConfigObject? {}", fieldType.toString(), isNestedConfigObject);
//
//            if (isNestedConfigObject) {
//                LOGGER.debug("Looking up factory method for {}", fieldType);
//                Method createMethod = field.getType().getDeclaredMethod("create");
//                createMethod.setAccessible(true);
//                LOGGER.debug("Looked up factory method for {}", fieldType);
//
//                LOGGER.debug("Creating instance of {}", fieldType);
//                Object nested = createMethod.invoke(null);
//
//                LOGGER.debug("Created instance of {}", fieldType);
//                field.set(obj, nested);
//                return Optional.of(nested);
//            }
            return Optional.empty();
        };
        return ReflectCallback.execute(reflectCallback);
    }

    static <T> T createInstance(Class<T> type) {

        return ReflectCallback.execute(() -> {
            Method factoryMethod = type.getDeclaredMethod("create");
            factoryMethod.setAccessible(true);
            
            return (T) factoryMethod.invoke(null);
        });


    }
    
    
    static Class classForName(String classname) {
     return ReflectCallback.execute(() -> Class.forName(classname));
    }

}
