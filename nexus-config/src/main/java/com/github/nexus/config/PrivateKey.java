package com.github.nexus.config;

import java.nio.file.Path;

public interface PrivateKey {
    
    Path getPath();
    
    String getValue();
    
    PrivateKeyType getType();
    
    String getPassword();

    String getSnonce();
    
    String getAsalt();
    
    String getSbox();
    
    ArgonOptions getArgonOptions();
    
}
