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
import java.util.ArrayList;
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
        List<String> values = Arrays.asList(value);
        Object obj = config;
        for (int i = 0; i < pathTokens.length; i++) {

            final String fieldName = pathTokens[i];
            final Class type = obj.getClass();
            final Field field = resolveField(type, fieldName);
            field.setAccessible(true);

            if (Collection.class.isAssignableFrom(field.getType())) {
                final Class genericType = resolveCollectionParameter(field.getGenericType());
                if (isSimple(genericType)) {
                    if (String.class.equals(genericType)) {
                        setValue(obj, field, values);
                    }
                    if (Path.class.equals(genericType)) {
                        List<Path> paths = values.stream()
                                .map(s -> Paths.get(s))
                                .collect(Collectors.toList());

                        setValue(obj, field, paths);
                    }
                } else {

                    String nextFieldName = pathTokens[i + 1];
                    Field targetField = resolveField(genericType, nextFieldName);
                    targetField.setAccessible(true);

                    List list = (List) getValue(obj, field);
                    for (String v : values) {
                        Object instance = createInstance(genericType);
                        
                        Object convertedValue = convertTo(targetField.getType(), v);
                        if(!isSimple(targetField)) {
                            //TODO: 
                            String nestedPropertyName = pathTokens[i + 2];
                            Field nestedField = resolveField(targetField.getType(), nestedPropertyName);
                            nestedField.setAccessible(true);
                            Object convertedNestedValue = convertTo(nestedField.getType(), v);
                            setValue(convertedValue, nestedField, convertedNestedValue);
                        } 
                        
                        setValue(instance, targetField, convertedValue);

                        list.add(instance);
                    }
                    setValue(obj, field, list);
                    i = i + 2;

                }

                LOGGER.debug("{} is a List<{}>", fieldName, genericType.getSimpleName());
            } else {
                obj = setProperty(obj, field, values).orElse(null);
            }

        }
    }

    static <T> T getValue(Object from, Field field) {
        return ReflectCallback.execute(() -> {
            return (T) field.get(from);
        });
    }

    static void setValue(Object obj, Field field, Object value) {
        ReflectCallback.execute(() -> {
            field.set(obj, value);
            return null;
        });
    }

    static Field resolveField(Class type, String name) {
        LOGGER.debug("Resolving {}#{}",type,name);
        
        return Stream.of(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(XmlElement.class))
                .filter(f -> f.getAnnotation(XmlElement.class).name().equals(name))
                .findAny()
                .orElseGet(() -> ReflectCallback.execute(() -> type.getDeclaredField(name)));
    }

    static Optional<Object> setProperty(Object obj, Field field, List<String> values) {
        LOGGER.debug("setProperty:  {} , {}", field, values);
        ReflectCallback<Optional<Object>> reflectCallback = () -> {
            final Class type = obj.getClass();
            final Class fieldType = field.getType();

            LOGGER.debug("Resolved field name: {}#{}, type: {}", fieldType, field.getName(), fieldType.getName());

            if (isSimple(fieldType)) {
                final String value = values.get(0);
                final Object convertedValue;
                //FIXME::
                if (fieldType == boolean.class) {
                    convertedValue = convertTo(Boolean.class, value);
                } else {
                    convertedValue = convertTo(fieldType, value);
                }
                field.set(obj, convertedValue);
                return Optional.of(obj);
            }

            boolean isNestedConfigObject
                    = fieldType.getPackage().getName().startsWith("com.quorum.tessera");

            Object existingValue = field.get(obj);

            return Optional.ofNullable(existingValue);
        };

        return ReflectCallback.execute(reflectCallback);
    }

    static <T> T createInstance(Class<T> type) {

        return ReflectCallback.execute(() -> {
            Method factoryMethod = type.getDeclaredMethod("create");
            factoryMethod.setAccessible(true);
            final Object instance = factoryMethod.invoke(null);
            initialiseNestedObjects(instance);
            return (T) instance;
        });

    }

    static Class classForName(String classname) {
        return ReflectCallback.execute(() -> Class.forName(classname));
    }

    static void initialiseNestedObjects(Object obj) {
        ReflectCallback.execute(() -> {
            Class type = obj.getClass();
            Field[] fields = type.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Class fieldType = field.getType();
                if (isSimple(fieldType)) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(fieldType)) {
                    setValue(obj, field, new ArrayList<>());
                    continue;
                }

                Object nestedObject = createInstance(fieldType);
                initialiseNestedObjects(nestedObject);
                setValue(obj, field, nestedObject);

            }
            return null;
        });

    }

    static <T> T convertTo(Class<T> type, String value) {

        if (Objects.isNull(value)) {
            return null;
        }

        if (String.class.equals(type)) {
            return (T) value;
        }

        if (Path.class.equals(type)) {
            return (T) Paths.get(value);
        }

        if (type.isEnum()) {
            return (T) Enum.valueOf(type.asSubclass(Enum.class), value);
        }

        if(type.getPackage().getName().startsWith("com.quorum.tessera")) {
            return createInstance(type);
        }
        
        
        return SIMPLE_TYPES.stream()
                .filter(t -> t.equals(type))
                .findFirst()
                .map(c -> ReflectCallback.execute(() -> c.getDeclaredMethod("valueOf", String.class)))
                .map(m -> ReflectCallback.execute(() -> m.invoke(null, value)))
                .map(type::cast)
                .get();

    }

}
