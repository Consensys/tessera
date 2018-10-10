
package com.quorum.tessera.transaction;

import com.quorum.tessera.exception.TesseraException;


public class InvalidRecipientException extends TesseraException {

    public InvalidRecipientException(String message) {
        super(message);
    }
    
}
