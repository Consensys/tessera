package com.quorum.tessera.test;

import db.DatabaseServer;
import db.HsqlDatabaseServer;

public enum DBType {
    
    H2("jdbc:h2:./target/h2/%s%d;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0;AUTO_SERVER=TRUE"), 
    HSQL("jdbc:hsqldb:hsql://127.0.0.1:9189/%s%d"), 
    SQLITE("jdbc:sqlite:target/sqlite-%s%d.db");

    private final String urlTemplate;

    DBType(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }
    
    public String createUrl(String nodeId,int nodeNumber) {
        return String.format(urlTemplate,nodeId,nodeNumber);
    }

    public DatabaseServer createDatabaseServer(String nodeId) {
        
        if(this == HSQL) {
            return new HsqlDatabaseServer(nodeId);
        }
        
        return new DatabaseServer() {};
    }
    
}
