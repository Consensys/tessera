package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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
    public void getTypeReturnsFilesystem() {
        FilesystemKeyPair keyPair = new FilesystemKeyPair(Paths.get("path1"), Paths.get("path2"));
        assertThat(keyPair.getType()).isEqualByComparingTo(ConfigKeyPairType.FILESYSTEM);
    }

}
