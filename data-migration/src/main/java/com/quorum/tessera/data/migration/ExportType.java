
package com.quorum.tessera.data.migration;

import java.sql.Driver;


public enum ExportType {
    H2(org.h2.Driver.class),
    SQLITE(org.sqlite.JDBC.class);
    
    final Class<? extends Driver> driver;

    ExportType(Class<? extends Driver> driver) {
        this.driver = driver;
    }
    
    
    
}
