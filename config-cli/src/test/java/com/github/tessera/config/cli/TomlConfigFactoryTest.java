package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.SslTrustMode;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TomlConfigFactoryTest {

    private TomlConfigFactory tomlConfigFactory;

    @Before
    public void onSetup() throws Exception {
        tomlConfigFactory = new TomlConfigFactory();
    }

    @Test
    public void createConfigFromSampleFile() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {
            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();
        }
    }

    @Test
    public void createConfigFromSampleFileAndAddedPasswordsFile() throws IOException {

        Path passwordsFile = Files.createTempFile("createConfigFromSampleFileAndAddedPasswordsFile", ".txt");

        List<String> passwordsFileLines = Arrays.asList("PASSWORD_1", "PASSWORD_2", "PASSWORD_3");
        
        Files.write(passwordsFile, passwordsFileLines);
        
        
        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {

            List<String> lines = Stream.of(configData)
                    .map(InputStreamReader::new)
                    .map(BufferedReader::new)
                    .flatMap(BufferedReader::lines)
                    .collect(Collectors.toList());
            
            lines.add(String.format("passwords = \"%s\"",passwordsFile.toString()));
            
            
            String dataS = String.join(System.lineSeparator(), lines);
            System.out.println(dataS);
            
            final byte[] data = String.join(System.lineSeparator(), lines).getBytes();
            try (InputStream ammendedInput = new ByteArrayInputStream(data)) {
                Config result = tomlConfigFactory.create(ammendedInput);
                assertThat(result).isNotNull();
            }
        }

        Files.deleteIfExists(passwordsFile);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeysNotSupported() throws IOException {
        InputStream configData = mock(InputStream.class);
        InputStream keyConfigData = mock(InputStream.class);

        tomlConfigFactory.create(configData, keyConfigData);
    }

    @Test
    public void resolveSslTrustModeForCaOrTofu() {
        SslTrustMode result = TomlConfigFactory.resolve("ca-or-tofu");
        assertThat(result).isIn(SslTrustMode.CA, SslTrustMode.TOFU);

    }

    @Test
    public void resolveSslTrustMode() {
        for (SslTrustMode mode : SslTrustMode.values()) {
            SslTrustMode result = TomlConfigFactory.resolve(mode.name().toLowerCase());
            assertThat(result).isEqualTo(mode);
        }
    }

    @Test
    public void resolveSslTrustModeNone() {

        assertThat(TomlConfigFactory.resolve(null)).isEqualTo(SslTrustMode.NONE);
        assertThat(TomlConfigFactory.resolve("BOGUS")).isEqualTo(SslTrustMode.NONE);
    }

}
