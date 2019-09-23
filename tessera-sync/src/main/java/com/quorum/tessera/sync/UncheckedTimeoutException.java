
package com.quorum.tessera.sync;

import java.util.concurrent.TimeoutException;


public class UncheckedTimeoutException extends RuntimeException {

    public UncheckedTimeoutException(TimeoutException timeoutException) {
        super(timeoutException);
    }
    
    
}
