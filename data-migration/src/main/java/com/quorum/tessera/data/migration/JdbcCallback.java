
package com.quorum.tessera.data.migration;

import java.sql.SQLException;

@FunctionalInterface
public interface JdbcCallback<T> {
    
    T doExecute() throws SQLException;
    
    static <T> T execute(JdbcCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (SQLException ex) {
            throw new StoreLoaderException(ex);
        }
    }
    
}
