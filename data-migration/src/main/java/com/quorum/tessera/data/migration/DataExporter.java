package com.quorum.tessera.data.migration;

import java.util.Map;
import java.util.ServiceLoader;

public interface DataExporter {
    
    void export(Map<byte[], byte[]> data);

    static DataExporter create() {
        return ServiceLoader.load(DataExporter.class).iterator().next();
    }
    
}
