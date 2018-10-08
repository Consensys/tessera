package com.quorum.tessera.config.keypairs;

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
    public void gettersWorkAsExpected() {
        Path pub = Paths.get("pubPath");
        Path priv = Paths.get("privPath");

        FilesystemKeyPair keyPair = new FilesystemKeyPair(pub, priv);

        assertThat(keyPair.getPublicKeyPath()).isEqualByComparingTo(pub);
        assertThat(keyPair.getPrivateKeyPath()).isEqualByComparingTo(priv);
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

}
