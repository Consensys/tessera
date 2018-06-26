package com.github.nexus.ssl.strategy;

public enum AuthenticationMode {
    STRICT, OFF;

    public static AuthenticationMode getValue(String value){
        try {
            return AuthenticationMode.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex){
            return AuthenticationMode.OFF;
        }
    }
}
