package com.github.tessera.config.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class LegacyCliAdapterTest {

    private final LegacyCliAdapter instance = new LegacyCliAdapter();

    @Test
    public void help() throws Exception {

        CliResult result = instance.execute("--help");
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);

    }

    @Test
    public void noOptions() throws Exception {
        
        Path sampleFile = Paths.get(getClass().getResource("/sample.conf").toURI());
        Path configFile = Files.createTempFile("noOptions", ".txt");

        Files.write(configFile, Files.readAllBytes(sampleFile));

        CliResult result = instance.execute(configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(1);

        Files.deleteIfExists(configFile);

    }
    
    

}
