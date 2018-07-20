
package com.github.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public interface StoreLoader {
    
    Map<byte[],byte[]> load(Path input) throws IOException;
    
}
