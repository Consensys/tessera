package com.github.tessera.data.migration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class MainTest {

    @Test
    public void executeMain() throws Exception {

        Path outputFile = Files.createTempFile("somename", ".txt");

        String[] args = new String[]{
            "-inputfile", getClass().getResource("/bdb-sample.txt").getFile(),
            "-outputfile", outputFile.toString()
        };
        Main.main(args);

        List<String> results = Files.readAllLines(outputFile);

        assertThat(results).hasSize(12);
    }

    @Test
    public void executeMainNotOutputFile() throws Exception {
        Path inputFile = Paths.get(getClass().getResource("/bdb-sample.txt").toURI());
        String[] args = new String[]{
            "-inputfile", inputFile.toString(),};
        Main.main(args);

        assertThat(inputFile).isNotNull();
    }

    @Test
    public void constcutorThrowsUnsupportedException() throws Exception {
        Constructor c = Main.class.getDeclaredConstructor();
        c.setAccessible(true);
        try {
            c.newInstance();
            failBecauseExceptionWasNotThrown(InvocationTargetException.class);
        } catch (InvocationTargetException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(UnsupportedOperationException.class);

        }
    }

}
