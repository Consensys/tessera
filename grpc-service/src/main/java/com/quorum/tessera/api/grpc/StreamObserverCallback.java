
package com.quorum.tessera.api.grpc;


@FunctionalInterface
public interface StreamObserverCallback<T> {
    
    T execute() throws Throwable;
    
}
