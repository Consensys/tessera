
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import org.junit.Test;


public class DefaultDataExporterTest {
    
    @Test
    public void doStuff() throws SQLException,IOException {
        DefaultDataExporter instance = new DefaultDataExporter();
        instance.export(Collections.emptyMap(),null);
    }
    
    
}
