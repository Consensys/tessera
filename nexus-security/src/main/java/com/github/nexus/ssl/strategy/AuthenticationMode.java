package com.github.nexus.ssl.strategy;

public enum AuthenticationMode {
    strict, off;

    public static AuthenticationMode getValue(String value){
        try {
            return AuthenticationMode.valueOf(value);
        }
        catch (IllegalArgumentException ex){
            return AuthenticationMode.off;
        }
    }
}
