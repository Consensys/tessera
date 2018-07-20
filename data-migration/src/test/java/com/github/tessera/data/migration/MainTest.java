package com.github.tessera.data.migration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.MissingOptionException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class MainTest {
    
    @Test
    public void noOptions() throws Exception {
        
        String[] args = new String[]{};
        
        try {
            Main.main(args);
            failBecauseExceptionWasNotThrown(MissingOptionException.class);
        } catch (MissingOptionException ex) {
            assertThat(ex.getMissingOptions()).hasSize(2);
            assertThat(ex.getMissingOptions()).containsExactly("storetype", "inputpath");
        }
        
    }
    
    @Test
    public void missingStoreTypeOption() throws Exception {
        
        String[] args = new String[]{
            "-inputpath", "somefile.txt"
        };
        
        try {
            Main.main(args);
            failBecauseExceptionWasNotThrown(MissingOptionException.class);
        } catch (MissingOptionException ex) {
            assertThat(ex.getMissingOptions()).hasSize(1);
            assertThat(ex.getMissingOptions()).containsExactly("storetype");
        }
        
    }
    
    @Test
    public void missingInputFileOption() throws Exception {
        
        String[] args = new String[]{
            "-storetype", "bdb"
        };
        
        try {
            Main.main(args);
            failBecauseExceptionWasNotThrown(MissingOptionException.class);
        } catch (MissingOptionException ex) {
            assertThat(ex.getMissingOptions()).hasSize(1);
            assertThat(ex.getMissingOptions()).containsExactly("inputpath");
        }
        
    }
    
    @Test
    public void bdbStoreType() throws Exception {
        
        Path inputFile = Paths.get(getClass().getResource("/bdb/bdb-sample.txt").toURI());
        
        Path outputPath = Files.createTempFile("bdbStoreType", ".txt");
        Files.deleteIfExists(outputPath);
        
        String[] args = new String[]{
            "-storetype", "bdb",
            "-inputpath", inputFile.toString(),
            "-outputfile", outputPath.toString()
        };
        
        Main.main(args);

        // assertThat(Files.lines(outputPath).count()).isEqualTo(22);
    }
    
    @Test
    public void dirStoreType() throws Exception {
        
        Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());
        
        String[] args = new String[]{
            "-storetype", "dir",
            "-inputpath", inputFile.toString()
        };
        
        Main.main(args);
        
    }
    
    @Test
    public void cannotBeConstructed() throws Exception {
        
        Constructor constructor = Main.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            failBecauseExceptionWasNotThrown(InvocationTargetException.class);
        } catch (InvocationTargetException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(UnsupportedOperationException.class);
        }
        
    }
    
}
