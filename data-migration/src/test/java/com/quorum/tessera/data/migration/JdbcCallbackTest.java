package com.quorum.tessera.data.migration;

import org.junit.Test;

import java.sql.SQLException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class JdbcCallbackTest {
    
    @Test(expected = StoreLoaderException.class)
    public void executeThrowsSQLException() throws Exception {
    
        JdbcCallback callback = mock(JdbcCallback.class);
        
        doThrow(SQLException.class).when(callback).doExecute();
        
        JdbcCallback.execute(callback);
    }
    
}
