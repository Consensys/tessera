
package com.quorum.tessera.reflect;

@FunctionalInterface
public interface ReflectCallback<T>  {
    
    T doExecute() throws ClassNotFoundException;
    
    
    static <T> T execute(ReflectCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (ClassNotFoundException ex) {
            throw new ReflectException(ex);
        }
    
    }
        
    
}
