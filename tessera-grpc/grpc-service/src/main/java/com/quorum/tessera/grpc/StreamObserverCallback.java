package com.quorum.tessera.grpc;

@FunctionalInterface
public interface StreamObserverCallback<T> {
    
    T execute() throws Throwable;
    
}
