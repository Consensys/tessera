package com.quorum.tessera.data.migration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class BdbDumpFileTest {

    @Test
    public void loadSample() throws URISyntaxException, IOException {

        Path inputFile = Paths.get(getClass().getResource("/bdb/bdb-sample.txt").toURI());

        Map<byte[], byte[]> results = new BdbDumpFile().load(inputFile);

        assertThat(results).hasSize(12);

        assertThat(results.keySet().stream().anyMatch(Objects::isNull)).isFalse();
        assertThat(results.values().stream().anyMatch(Objects::isNull)).isFalse();
    }

    @Test
    public void loadSimpleEntrySample() throws URISyntaxException, IOException {

        Path inputFile = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());

        Map<byte[], byte[]> results = new BdbDumpFile().load(inputFile);

        assertThat(results).hasSize(1);

        assertThat(results.keySet().stream().anyMatch(Objects::isNull)).isFalse();
        assertThat(results.values().stream().anyMatch(Objects::isNull)).isFalse();
    }

}
