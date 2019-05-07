package com.quorum.tessera.data.migration;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class MockDataLoader implements StoreLoader {

    private final Iterator<Map.Entry<String, String>> iterator;

    public MockDataLoader(final Map<String, String> data) {
        this.iterator = data.entrySet().iterator();
    }

    @Override
    public void load(final Path input) {
        //no-op
    }

    @Override
    public DataEntry nextEntry() {
        if(!iterator.hasNext()) {
            return null;
        }

        final Map.Entry<String, String> next = iterator.next();

        return new DataEntry(next.getKey().getBytes(), new ByteArrayInputStream(next.getValue().getBytes()));
    }

}
