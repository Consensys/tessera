package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OverrideUtil {

    Set<String> ADDITIVE_COLLECTION_FIELDS = Stream.of("peers").collect(Collectors.toSet());

    Logger LOGGER = LoggerFactory.getLogger(OverrideUtil.class);

    List<Class> SIMPLE_TYPES = Collections.unmodifiableList(
            Arrays.asList(String.class, Path.class, Integer.class, Boolean.class, Long.class));

    Map<Class<?>, Class<?>> PRIMATIVE_LOOKUP = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        {
            put(Boolean.TYPE, Boolean.class);
            put(Byte.TYPE, Byte.class);
            put(Character.TYPE, Character.class);
            put(Short.TYPE, Short.class);
            put(Integer.TYPE, Integer.class);
            put(Long.TYPE, Long.class);
            put(Double.TYPE, Double.class);
            put(Float.TYPE, Float.class);
            put(Void.TYPE, Void.TYPE);
        }
    });

    static Map<String, Class> buildConfigOptions() {
        final Map<String, Class> fields = fields(null, Config.class);

        //add key overrides separately as they cannot be determined from the type directly
        fields.put("keys.keyData.privateKeyPath", Path.class);
        fields.put("keys.keyData.config.data.aopts.parallelism", String.class);
        fields.put("keys.keyData.config.data.aopts.memory", String.class);
        fields.put("keys.keyData.config.data.aopts.iterations", String.class);
        fields.put("keys.keyData.config.data.aopts.algorithm", String.class);

        return fields;
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

        if (type.isPrimitive()) {
            return true;
        }

        if (type.getPackage() == null) {
            return true;
        }

        if (SIMPLE_TYPES.contains(type)) {
            return true;
        }

        return type.isEnum();
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

    /**
     * Directly set field values using reflection.
     *
     * @param root
     * @param path
     * @param value
     */
    static void setValue(Object root, String path, String... value) {

        if(root == null) {
            return;
        }

        final ListIterator<String> pathTokens = Arrays.asList(path.split("\\.")).listIterator();

        final Class rootType = root.getClass();

        while (pathTokens.hasNext()) {

            final String token = pathTokens.next();
            final Field field = resolveField(rootType, token);
            field.setAccessible(true);

            final Class fieldType = field.getType();

            if (Collection.class.isAssignableFrom(fieldType)) {

                final Class genericType = resolveCollectionParameter(field.getGenericType());

                List list = (List) Optional.ofNullable(getValue(root, field))
                        .orElse(new ArrayList<>());
                if (isSimple(genericType)) {

                    List convertedValues = (List) Stream.of(value)
                            .map(v -> convertTo(genericType, v))
                            .collect(Collectors.toList());

                    List merged = new ArrayList(list);
                    merged.addAll(convertedValues);

                    setValue(root, field, merged);

                } else {

                    List<String> builder = new ArrayList<>();
                    pathTokens.forEachRemaining(builder::add);
                    String nestedPath = builder.stream().collect(Collectors.joining("."));
                    
                    final Object[] newList;
                    if(ADDITIVE_COLLECTION_FIELDS.contains(field.getName())) {
                        newList = new Object[value.length];
                    } else {
                        newList = Arrays.copyOf(list.toArray(), value.length);
                    }

                    for (int i = 0; i < value.length; i++) {
                        final String v = value[i];

                        final Object nestedObject = Optional.ofNullable(newList[i])
                                .orElse(createInstance(genericType));

                        initialiseNestedObjects(nestedObject);

                        setValue(nestedObject, nestedPath, v);
                        newList[i] = nestedObject;
                    }
                    List merged = new ArrayList();
                    if(ADDITIVE_COLLECTION_FIELDS.contains(field.getName())) {
                        merged.addAll(list);
                    }
                    merged.addAll(Arrays.asList(newList));
                    setValue(root, field, merged);

                }

            } else if (isSimple(fieldType)) {
                Class convertedType = PRIMATIVE_LOOKUP.getOrDefault(fieldType, fieldType);
                Object convertedValue = convertTo(convertedType, value[0]);
                setValue(root, field, convertedValue);

            } else {

                Object nestedObject = getOrCreate(root, field);
                List<String> builder = new ArrayList<>();
                pathTokens.forEachRemaining(builder::add);
                String nestedPath = builder.stream().collect(Collectors.joining("."));
                setValue(nestedObject, nestedPath, value);

                setValue(root, field, nestedObject);
            }

        }

    }

    static <T> T getOrCreate(Object from, Field field) {
        T value = getValue(from, field);
        return Optional.ofNullable(value)
                .orElse((T) createInstance(field.getType()));
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
        LOGGER.debug("Resolving {}#{}", type, name);

        return Stream.of(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(XmlElement.class))
                .filter(f -> f.getAnnotation(XmlElement.class).name().equals(name))
                .findAny()
                .orElseGet(() -> ReflectCallback.execute(() -> type.getDeclaredField(name)));
    }

    static <T> T createInstance(Class<T> type) {

        if(type.isInterface()) {
            return null;
        }

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
        if (obj == null) {
            return;
        }
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

        if (byte[].class.isAssignableFrom(type)) {
            return (T) value.getBytes(StandardCharsets.UTF_8);
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
