package com.github.nexus.config;


public interface PrivateKey {
    
    PrivateKeyType getType();
    
    String getPassword();
    
}
