
package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public interface StoreLoader {
    
    Map<byte[],byte[]> load(Path input) throws IOException;
    

    Map<StoreType,StoreLoader> LOOKUP = Collections.unmodifiableMap(new HashMap<StoreType,StoreLoader> () {{
        put(StoreType.BDB,new BdbDumpFile());
        put(StoreType.DIR,new DirectoryStoreFile());
        put(StoreType.SQLITE,new SqliteLoader());
    }});
    
    static StoreLoader create(StoreType storeType) {
        return Optional.ofNullable(LOOKUP.get(storeType)).get();
    }
    
}
