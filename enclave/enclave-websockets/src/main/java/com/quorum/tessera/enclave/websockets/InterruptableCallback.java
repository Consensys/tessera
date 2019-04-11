package com.quorum.tessera.enclave.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface InterruptableCallback<T> {
 
    T doExecute() throws InterruptedException;
    
    Logger LOGGER = LoggerFactory.getLogger(InterruptableCallback.class);
    
    static <T> T execute(InterruptableCallback<T> callback) {
        try{
            return callback.doExecute();
        } catch (InterruptedException ex) {
            LOGGER.warn("Thread interupted {}",ex.getMessage());
            return null;
        }
    }
    
}
