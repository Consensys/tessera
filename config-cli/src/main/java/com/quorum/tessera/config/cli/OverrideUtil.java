package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface OverrideUtil {

    static List<String> buildConfigOptions() {
        return fields("config", Config.class);
    }

    static Class resolveCollectionParameter(Type type) {

        return Stream.of(type)
                .map(ParameterizedType.class::cast)
                .map(pt -> pt.getActualTypeArguments()[0])
                .map(Type::getTypeName)
                .map(n -> ReflectCallback.execute(() -> Class.forName(n))).findAny().get();

    }

    static boolean isSimple(Field field) {
        
        
            if (field.getType().getPackage() == null) {
               return true;
            }

            if (field.getType().equals(String.class)) {
                return true;
            }

            if (field.getType().equals(Path.class)) {
                return true;
            }

            if (field.getType().equals(Integer.class)) {
                return true;
            }

            if (field.getType().isEnum()) {
                return true;
            }
            return false;
    }
    
    static List<String> fields(String prefix, Class type) {
        List<String> list = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {

            if (isSimple(field)) {
                list.add(prefix + "." + field.getName());
                continue;
            }

            if (field.getType().getPackage().equals(Config.class.getPackage())) {
                list.addAll(fields(prefix + "." + field.getName(), field.getType()));
            } 
            
            if(Collection.class.isAssignableFrom(field.getType())) {
              Class t =   resolveCollectionParameter(field.getGenericType());
               list.addAll(fields(prefix + "." + field.getName() +"[]",t));
            }
        }

        return list;

    }


}
