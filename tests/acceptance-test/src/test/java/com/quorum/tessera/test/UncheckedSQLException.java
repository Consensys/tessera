package com.quorum.tessera.test;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

    public UncheckedSQLException(SQLException cause) {
        super(cause);
    }
}
