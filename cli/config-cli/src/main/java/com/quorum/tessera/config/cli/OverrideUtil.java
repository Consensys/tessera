package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OverrideUtil {

  Set<String> ADDITIVE_COLLECTION_FIELDS = Stream.of("peers").collect(Collectors.toSet());

  Logger LOGGER = LoggerFactory.getLogger(OverrideUtil.class);

  List<Class> SIMPLE_TYPES =
      Collections.unmodifiableList(
          Arrays.asList(String.class, Path.class, Integer.class, Boolean.class, Long.class));

  Map<Class<?>, Class<?>> PRIMITIVE_LOOKUP =
      Collections.unmodifiableMap(
          new HashMap<Class<?>, Class<?>>() {
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
        .map(n -> ReflectCallback.execute(() -> Class.forName(n)))
        .findAny()
        .get();
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

    String amendedPrefix = Optional.ofNullable(prefix).map(s -> s.concat(".")).orElse("");

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
  static void setValue(Object root, String path, String value) {

    if (root == null) {
      return;
    }

    final ListIterator<String> pathTokens = Arrays.asList(path.split("\\.")).listIterator();

    final Class rootType = root.getClass();
    if (rootType.isAnonymousClass()) {
      return;
    }

    if (Map.class.isAssignableFrom(rootType)) {
      Map.class.cast(root).put(path, value);
      return;
    }

    while (pathTokens.hasNext()) {

      final String token = pathTokens.next();

      final String target;
      final String position;

      final String collectionPattern = "^(.*)\\[([0-9].*)\\]$";
      final Pattern r = Pattern.compile(collectionPattern);
      final Matcher m = r.matcher(token);

      if (m.matches()) {
        target = m.group(1);
        position = m.group(2);
        LOGGER.debug("Setting {} at position {}", target, position);
      } else {
        target = token;
        position = null;
      }

      final Field field = resolveField(rootType, target);
      field.setAccessible(true);

      final Class fieldType = field.getType();

      if (Collection.class.isAssignableFrom(fieldType)) {
        if (Objects.isNull(position)) {
          throw new CliException(
              path + ": position not provided for Collection parameter override " + token);
        }

        final int i = Integer.parseInt(position);

        final Class genericType = resolveCollectionParameter(field.getGenericType());

        List list = (List) Optional.ofNullable(getValue(root, field)).orElse(new ArrayList<>());

        if (isSimple(genericType)) {

          Object convertedValue = convertTo(genericType, value);

          List updated = new ArrayList(list);

          while (updated.size() <= i) {
            Class convertedType = PRIMITIVE_LOOKUP.getOrDefault(fieldType, fieldType);
            Object emptyValue = convertTo(convertedType, null);

            updated.add(emptyValue);
          }

          updated.set(i, convertedValue);

          setValue(root, field, updated);

        } else {

          List<String> builder = new ArrayList<>();
          pathTokens.forEachRemaining(builder::add);
          String nestedPath = builder.stream().collect(Collectors.joining("."));

          while (list.size() <= i) {
            final Object newObject = createInstance(genericType);
            initialiseNestedObjects(newObject);
            list.add(newObject);
          }

          final Object nestedObject = list.get(i);

          // update the collection's complex object
          setValue(nestedObject, nestedPath, value);

          // update the root object with the updated collection
          setValue(root, field, list);
        }

      } else if (isSimple(fieldType)) {
        Class convertedType = PRIMITIVE_LOOKUP.getOrDefault(fieldType, fieldType);
        Object convertedValue = convertTo(convertedType, value);
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
    return Optional.ofNullable(value).orElse((T) createInstance(field.getType()));
  }

  static <T> T getValue(Object from, Field field) {
    return ReflectCallback.execute(
        () -> {
          return (T) field.get(from);
        });
  }

  static void setValue(Object obj, Field field, Object value) {
    ReflectCallback.execute(
        () -> {
          field.set(obj, value);
          return null;
        });
  }

  static Field resolveField(Class type, String name) {
    LOGGER.debug("Resolving {}#{}", type, name);

    Predicate<Field> isJaxbElement = f -> f.isAnnotationPresent(XmlElement.class);
    Predicate<Field> isJaxbAttribute = f -> f.isAnnotationPresent(XmlAttribute.class);

    Predicate<Field> matchJaxbElementName =
        f -> f.getAnnotation(XmlElement.class).name().equals(name);
    Predicate<Field> matchJaxbAttributeName =
        f -> f.getAnnotation(XmlAttribute.class).name().equals(name);

    return Stream.of(type.getDeclaredFields())
        .filter(
            isJaxbElement.and(matchJaxbElementName).or(isJaxbAttribute.and(matchJaxbAttributeName)))
        .peek(f -> LOGGER.debug("Found field {} for type {}", f, type))
        .findAny()
        .orElseGet(
            () -> {
              return ReflectCallback.execute(
                  () -> {
                    LOGGER.debug("Find {} for type {}", name, type);
                    Field field = type.getDeclaredField(name);
                    LOGGER.debug("Found {} for type {}", name, type);
                    return field;
                  });
            });
  }

  static <T> T createInstance(Class<T> type) {

    if (Map.class.isAssignableFrom(type)) {
      return (T) new LinkedHashMap<>();
    }

    if (type.isInterface()) {
      return null;
    }

    return ReflectCallback.execute(
        () -> {
          final T instance = type.newInstance();
          initialiseNestedObjects(instance);
          return instance;
        });
  }

  static Class classForName(String classname) {
    return ReflectCallback.execute(() -> Class.forName(classname));
  }

  static void initialiseNestedObjects(Object obj) {
    if (obj == null) {
      return;
    }
    ReflectCallback.execute(
        () -> {
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

            if (Map.class.isAssignableFrom(fieldType)) {
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
      return (T) Enum.valueOf(type.asSubclass(Enum.class), value.toUpperCase());
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
