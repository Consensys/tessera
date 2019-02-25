package com.quorum.tessera.config;

public enum CommunicationType {
    REST, 
    GRPC, 
    @Deprecated
    UNIX_SOCKET, 
    WEB_SOCKET;
}
