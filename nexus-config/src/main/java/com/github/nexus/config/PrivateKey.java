package com.github.nexus.config;

import java.nio.file.Path;

public interface PrivateKey {
    
    Path getPath();
    
    String getValue();
    
    <T extends PrivateKeyType>  T getType();
    
    String getPassword();
    
}
