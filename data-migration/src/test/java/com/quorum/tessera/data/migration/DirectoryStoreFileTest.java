package com.quorum.tessera.data.migration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DirectoryStoreFileTest {
    
    @Test
    public void load() throws Exception {
    
        Path directory = Paths.get(getClass().getResource("/dir/").toURI());
        
        DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();
        
        Map<byte[],byte[]> results = directoryStoreFile.load(directory);
        
        assertThat(results).hasSize(22);
        
    }
    
}
