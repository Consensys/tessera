package com.quorum.tessera.exception;

public interface ExceptionUtil {
    
    static Throwable extractCause(Throwable ex) {
        if(ex.getCause() != null) {
            return extractCause(ex.getCause());
        }
        return ex;
    }
    
}
