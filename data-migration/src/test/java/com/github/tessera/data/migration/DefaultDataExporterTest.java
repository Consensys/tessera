
package com.github.tessera.data.migration;

import java.util.Collections;
import org.junit.Test;


public class DefaultDataExporterTest {
    
    @Test
    public void doStuff() {
        DefaultDataExporter instance = new DefaultDataExporter();
        instance.export(Collections.emptyMap());
    }
    
    
}
