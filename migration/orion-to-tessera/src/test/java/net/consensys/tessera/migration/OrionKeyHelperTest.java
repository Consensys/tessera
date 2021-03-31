package net.consensys.tessera.migration;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class OrionKeyHelperTest {

    private Path expectedKeyPath = Paths.get("./nodeKey.key.tessera");

    @After
    public void cleanup() throws IOException {
        Files.delete(expectedKeyPath);
    }

    @Test
    public void migrateTesseraKeysCorrectly() throws IOException {
        Path orionConfigFilePath = Paths.get("").resolve("orion.conf").toAbsolutePath();

        OrionKeyHelper.from(orionConfigFilePath);

        assertThat(expectedKeyPath).exists();

        KeyDataConfig savedKeyData = JaxbUtil.unmarshal(Files.newInputStream(expectedKeyPath), KeyDataConfig.class);

        // these values have been checked that they are correct
        assertThat(savedKeyData.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(savedKeyData.getAsalt()).isEqualTo("pb5bmtfeG9qQ2bb6PSJN0g==");
        assertThat(savedKeyData.getSbox()).isEqualTo("esjC8EeY108ZxAO2CChEGzAfxJf1o3l5XKvVOhyHwV9w9xUebdKjGcF20Ae/TVIN");
        assertThat(savedKeyData.getSnonce()).isEqualTo("pb5bmtfeG9qQ2bb6PSJN0rxzsVEKkl2v");
        assertThat(savedKeyData.getArgonOptions()).isEqualTo(new ArgonOptions("i", 3, 262144, 1));
        assertThat(savedKeyData.getValue()).isNull();
    }
}
