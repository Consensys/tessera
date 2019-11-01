package com.quorum.tessera.config.keypairs;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class FilesystemKeyPairTest {

    private InlineKeypair inlineKeypair;

    @Before
    public void onSetup() {
        inlineKeypair = mock(InlineKeypair.class);
    }

    @Test
    public void gettersWorkAsExpected() {
        Path pub = Paths.get("pubPath");
        Path priv = Paths.get("privPath");

        FilesystemKeyPair keyPair = new FilesystemKeyPair(pub, priv, inlineKeypair);

        assertThat(keyPair.getPublicKeyPath()).isEqualByComparingTo(pub);
        assertThat(keyPair.getPrivateKeyPath()).isEqualByComparingTo(priv);
    }

    @Test
    public void setPasswordIsRetrievable() throws IOException, URISyntaxException {
        final Path pubFile = Files.createTempFile(UUID.randomUUID().toString(), ".pub");
        final Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

        final String pub = "public";
        Files.write(pubFile, pub.getBytes());

        final FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(pubFile, privFile, inlineKeypair);
        filesystemKeyPair.withPassword("password");

        assertThat(filesystemKeyPair.getPassword()).isEqualTo("password");
    }

    @Test
    public void setPasswordIsRetrievableOnNullInlineKey() throws IOException, URISyntaxException {
        final Path pubFile =
                Files.createTempFile(UUID.randomUUID().toString(), ".pub").resolveSibling("nonexistantkey");
        final Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

        final FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(pubFile, privFile, inlineKeypair);
        filesystemKeyPair.withPassword("password");

        assertThat(filesystemKeyPair.getPassword()).isEqualTo("password");
    }

    @Test
    public void noDelegateInclinePair() {
        Path publicKeyPath = mock(Path.class);
        Path privateKeyPath = mock(Path.class);

        final FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);

        assertThat(filesystemKeyPair.getPublicKey()).isNull();
        assertThat(filesystemKeyPair.getInlineKeypair()).isNull();
        assertThat(filesystemKeyPair.getPrivateKey()).isNull();
        assertThat(filesystemKeyPair.getPrivateKeyPath()).isSameAs(privateKeyPath);
        assertThat(filesystemKeyPair.getPublicKeyPath()).isSameAs(publicKeyPath);

        verifyZeroInteractions(inlineKeypair);
    }

    @Test
    public void constructDefaultWithoutEncrptor() {
        Path publicKeyPath = mock(Path.class);
        Path privateKeyPath = mock(Path.class);
        FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(publicKeyPath, privateKeyPath);
        assertThat(filesystemKeyPair.getPublicKey()).isNull();
        assertThat(filesystemKeyPair.getInlineKeypair()).isNull();
        assertThat(filesystemKeyPair.getPrivateKey()).isNull();
        assertThat(filesystemKeyPair.getPrivateKeyPath()).isSameAs(privateKeyPath);
        assertThat(filesystemKeyPair.getPublicKeyPath()).isSameAs(publicKeyPath);

        verifyZeroInteractions(inlineKeypair);
    }
}
