package com.quorum.tessera.reflect;

import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ReflectCallback<T> {
    
    Logger LOGGER = LoggerFactory.getLogger(ReflectCallback.class);
    
    T doExecute() throws ClassNotFoundException,
            NoSuchFieldException,
            IllegalArgumentException,
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException;


    static <T> T execute(ReflectCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (NoSuchMethodException | InvocationTargetException
                | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException ex) {
            LOGGER.error(null, ex);
            throw new ReflectException(ex);
        }        
        
    }
    
}
