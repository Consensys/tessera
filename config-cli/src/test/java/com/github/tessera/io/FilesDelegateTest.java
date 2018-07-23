
package com.github.tessera.io;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class FilesDelegateTest {
    
    
    private final FilesDelegate filesDelegate = FilesDelegate.create();
    
    @Test
    public void lines() throws Exception {
        Path somefile = Files.createTempFile("FilesDelegateTest#lines", ".txt");
        
        try(BufferedWriter writer = Files.newBufferedWriter(somefile)) {
            writer.write("ONE");
            writer.newLine();
            writer.write("");
            writer.newLine();
            writer.write("THREE");
        }
        
        List<String> results = filesDelegate.lines(somefile).collect(Collectors.toList());
        try {
            assertThat(results).containsExactly("ONE","","THREE");
            
        } finally {
             Files.deleteIfExists(somefile);
        }
       
        
    }
}
