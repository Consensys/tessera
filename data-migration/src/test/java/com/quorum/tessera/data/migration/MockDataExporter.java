package com.quorum.tessera.data.migration;

import java.util.Map;

public class MockDataExporter implements DataExporter {
    
    private Map<byte[], byte[]> results;
    
    @Override
    public void export(Map<byte[], byte[]> data) {
        this.results = data;
    }

    public Map<byte[], byte[]> getResults() {
        return results;
    }
 
}
