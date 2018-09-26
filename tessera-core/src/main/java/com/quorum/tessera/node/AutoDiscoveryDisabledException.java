package com.quorum.tessera.node;

import com.quorum.tessera.exception.TesseraException;

public class AutoDiscoveryDisabledException extends TesseraException {

    public AutoDiscoveryDisabledException(String message) {
        super(message);
    }

}
