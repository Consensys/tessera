package com.quorum.tessera.cli;

public class CliException extends RuntimeException {

    public CliException(String message) {
        super(message);
    }

    public CliException(Throwable cause) {
        super(cause);
    }

}
