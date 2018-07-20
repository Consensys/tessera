package com.github.tessera.data.migration;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public interface DataExporter {
    
    void export(Map<byte[], byte[]> data);

    static DataExporter create() {
        Iterator<DataExporter> it = ServiceLoader.load(DataExporter.class).iterator();
        if(it.hasNext()) {
            return it.next();
        }
        return new DefaultDataExporter();
    }
    
}
