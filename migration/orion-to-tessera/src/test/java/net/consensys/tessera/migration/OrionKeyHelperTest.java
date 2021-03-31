package net.consensys.tessera.migration;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class OrionKeyHelperTest {

    @Test
    public void migrateTesseraKeysCorrectly() throws IOException {

        Path orionConfigFilePath = Paths.get("").resolve("orion.conf").toAbsolutePath();

        OrionKeyHelper.from(orionConfigFilePath);

        Path expectedKeyPath = Paths.get("./nodeKey.key.tessera");
        assertThat(expectedKeyPath).exists();

        Files.delete(expectedKeyPath);
    }
}
