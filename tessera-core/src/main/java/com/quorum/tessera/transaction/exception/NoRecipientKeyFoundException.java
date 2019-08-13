
package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;


public class NoRecipientKeyFoundException extends TesseraException {

    public NoRecipientKeyFoundException(String message) {
        super(message);
    }
    
}
