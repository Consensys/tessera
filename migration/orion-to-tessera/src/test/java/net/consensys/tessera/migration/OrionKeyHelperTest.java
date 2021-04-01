package net.consensys.tessera.migration;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrionKeyHelperTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private Path keyFile;

    private Path backupFile;

    private Path configFile;

    @Before
    public void beforeTest() throws Exception {

        keyFile = Paths.get(outputDir.getRoot().toPath().toString(),"junit.key");
        Files.createFile(keyFile);

        Path keyData = Paths.get(getClass().getResource("/nodeKey.key").toURI());
        Files.write(keyFile, Files.readAllBytes(keyData));
        assertThat(keyFile).exists();

        backupFile = Paths.get(outputDir.getRoot().toPath().toString(),"junit.key.orion");
        assertThat(backupFile).doesNotExist();

        Path passwordFile = Paths.get(outputDir.getRoot().toPath().toString(),"junitPasswordFile");
        Files.writeString(passwordFile,"orion");

        Path publicKeyFile = Paths.get(outputDir.getRoot().toPath().toString(),"junit.pub");
        Files.createFile(publicKeyFile);
        Files.writeString(publicKeyFile,"arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg=");

        Toml toml = new Toml();
        Map env = Map.of(
            "workdir",outputDir.getRoot().toPath().toAbsolutePath().toString(),
            "privatekeys",new String[] {"junit.key"},
            "publickeys",new String[] {"junit.pub"},
            "passwords","junitPasswordFile"
        );

        configFile = outputDir.getRoot().toPath().resolve("junit-orion.conf");

        TomlWriter tomlWriter = new TomlWriter();
        try(OutputStream outputStream = new TeeOutputStream(Files.newOutputStream(configFile))) {
            tomlWriter.write(env, outputStream);
        }

    }


    @Test
    public void migrateTesseraKeysCorrectly() throws IOException {

        OrionKeyHelper.from(configFile);

        assertThat(backupFile).exists();

        try(InputStream inputStream = Files.newInputStream(keyFile)) {
            KeyDataConfig savedKeyData = JaxbUtil.unmarshal(inputStream, KeyDataConfig.class);

            // these values have been checked that they are correct
            assertThat(savedKeyData.getType()).isEqualTo(PrivateKeyType.LOCKED);
            assertThat(savedKeyData.getAsalt()).isEqualTo("pb5bmtfeG9qQ2bb6PSJN0g==");
            assertThat(savedKeyData.getSbox()).isEqualTo("esjC8EeY108ZxAO2CChEGzAfxJf1o3l5XKvVOhyHwV9w9xUebdKjGcF20Ae/TVIN");
            assertThat(savedKeyData.getSnonce()).isEqualTo("pb5bmtfeG9qQ2bb6PSJN0rxzsVEKkl2v");
            assertThat(savedKeyData.getArgonOptions()).isEqualTo(new ArgonOptions("i", 3, 262144, 1));
            assertThat(savedKeyData.getValue()).isNull();
        }
    }
}
