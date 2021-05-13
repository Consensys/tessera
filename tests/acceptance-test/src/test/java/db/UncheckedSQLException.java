package db;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

  public UncheckedSQLException(final SQLException cause) {
    super(cause);
  }
}
