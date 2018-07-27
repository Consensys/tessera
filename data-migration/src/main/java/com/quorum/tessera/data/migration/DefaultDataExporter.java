
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;


public class DefaultDataExporter implements DataExporter {

    @Override
    public void export(Map<byte[], byte[]> data,Path output)  throws SQLException,IOException  {
       
    }


    
}
