package com.github.nexus.config;


public interface Peer {
    
    String getUrl();
    
    <P extends PublicKey> P getPublicKey();
    
}
