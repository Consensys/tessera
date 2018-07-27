
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;


public class DefaultDataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data)  throws SQLException,IOException  {
       
    }

    @Override
    public String dbId() {
        return "default";
    }
    
}
