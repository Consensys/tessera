
package com.quorum.tessera.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    
    @Test
    public void newInputStream() throws Exception {
        
        Path somefile = Files.createTempFile("FilesDelegateTest#newInputStream", ".txt");
        
        Files.write(somefile, Arrays.asList("SOMEDATA"));
        
        InputStream result = filesDelegate.newInputStream(somefile);
        
        assertThat(result).isNotNull();
        
        List<String> tokens = Stream.of(result)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines)
                .collect(Collectors.toList());
        
        assertThat(tokens).containsExactly("SOMEDATA");

        
        Files.deleteIfExists(somefile);
   
    }
}
