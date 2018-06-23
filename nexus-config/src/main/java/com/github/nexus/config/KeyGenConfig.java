package com.github.nexus.config;

import java.nio.file.Path;

public interface KeyGenConfig {
    
    Path getBasePath();
    
    boolean isGenerateIfMissing();
    
}
