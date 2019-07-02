package com.quorum.tessera.transaction;

import com.quorum.tessera.exception.TesseraException;

public class NoRecipientKeyFoundException extends TesseraException {

    public NoRecipientKeyFoundException(String message) {
        super(message);
    }
}
