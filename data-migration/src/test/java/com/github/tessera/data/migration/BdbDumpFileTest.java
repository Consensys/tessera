package com.github.tessera.data.migration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BdbDumpFileTest {

    private BdbDumpFile instance;

    private Path inputFile;

    public BdbDumpFileTest() {
    }

    @Before
    public void setUp() throws Exception {
        inputFile = Paths.get(getClass().getResource("/bdb-sample.txt").toURI());
        instance = new BdbDumpFile(inputFile);
    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void execute() throws IOException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            instance.execute(outputStream);

            List<String> results = Arrays.asList(outputStream.toString().split(System.lineSeparator()));

            assertThat(results).hasSize(12);

        }

    }

}
