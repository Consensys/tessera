package com.github.nexus.node.model;

public enum ClientAuthMode {
    strict, off;

    public static ClientAuthMode getValue(String value){
        try {
            return ClientAuthMode.valueOf(value);
        }
        catch (IllegalArgumentException ex){
            return ClientAuthMode.off;
        }
    }
}
