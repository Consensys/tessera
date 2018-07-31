package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;

public interface OverrideUtil {

    static Map<String, Class> buildConfigOptions() {
        return fields(null, Config.class);
    }

    //FIXME: Need to change @XmlElement#name()
    Map<Class, String> ELEMENT_NAME_ALIASES = new HashMap<Class, String>() {
        {
            put(SslConfig.class, "ssl");
            put(InfluxConfig.class, "influx");
        }
    };

    static String resolveName(Field field) {
        if (!field.isAnnotationPresent(XmlElement.class)) {
            return field.getName();
        }
        XmlElement xmlElement = field.getAnnotation(XmlElement.class);

        String name = ELEMENT_NAME_ALIASES.getOrDefault(field.getType(), xmlElement.name());

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
                    list.put(amendedPrefix + resolveName(field) + "[]", t);
                } else {
                    list.putAll(fields(amendedPrefix + resolveName(field) + "[]", t));
                }
            }
        }

        return list;

    }

}
