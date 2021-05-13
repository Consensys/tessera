package com.quorum.tessera.data.migration;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import org.junit.Test;

public class JdbcCallbackTest {

  @Test(expected = StoreLoaderException.class)
  public void executeThrowsSQLException() throws Exception {

    JdbcCallback callback = mock(JdbcCallback.class);

    doThrow(SQLException.class).when(callback).doExecute();

    JdbcCallback.execute(callback);
  }
}
