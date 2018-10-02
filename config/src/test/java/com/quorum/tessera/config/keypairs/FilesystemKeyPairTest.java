package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesystemKeyPairTest {

    @Test
    public void marshallingSetsCorrectFields() {

        final Path publicPath = Paths.get("public");
        final Path privatePath = Paths.get("private");

        final FilesystemKeyPair keyPair = new FilesystemKeyPair(publicPath, privatePath);

        final KeyData result = keyPair.marshal();

        assertThat(result.getConfig()).isNull();
        assertThat(result.getPrivateKey()).isNull();
        assertThat(result.getPublicKey()).isNull();

        assertThat(result.getPublicKeyPath()).isEqualTo(publicPath);
        assertThat(result.getPrivateKeyPath()).isEqualTo(privatePath);

    }

    @Test
    public void getInlineKeypairReturnsKeysReadFromFile() throws Exception {

        Path pubFile = Files.createTempFile(UUID.randomUUID().toString(), ".pub");
        Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

        pubFile.toFile().deleteOnExit();
        String pub = "public";
        Files.write(pubFile, pub.getBytes());

        FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(pubFile, privFile);

        InlineKeypair result = filesystemKeyPair.getInlineKeypair();

        KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null, null),
            PrivateKeyType.UNLOCKED
        );

        InlineKeypair expected = new InlineKeypair(pub, privKeyDataConfig);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void getTypeReturnsFilesystem() {
        FilesystemKeyPair keyPair = new FilesystemKeyPair(Paths.get("path1"), Paths.get("path2"));
        assertThat(keyPair.getType()).isEqualByComparingTo(ConfigKeyPairType.FILESYSTEM);
    }
}
