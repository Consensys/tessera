package com.github.nexus.config;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.github.nexus.config.PrivateKeyType.UNLOCKED;
import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyTest {

    @Test
    public void legacyKeyPresentPrefersThatValue() {

        final LegacyPrivateKeyFile keyFile = new LegacyPrivateKeyFile(
            "bytes", UNLOCKED, new ArgonOptions("id", 1, 1, 1), "snonce", "asalt", "sbox"
        );

        final PrivateKey privateKey = new PrivateKey(keyFile, null, null, null, null, null, null, null, null);

        assertThat(privateKey.getArgonOptions()).isEqualToComparingFieldByField(new ArgonOptions("id", 1, 1, 1));
        assertThat(privateKey.getSbox()).isEqualTo("sbox");
        assertThat(privateKey.getAsalt()).isEqualTo("asalt");
        assertThat(privateKey.getSnonce()).isEqualTo("snonce");
        assertThat(privateKey.getType()).isEqualTo(UNLOCKED);
        assertThat(privateKey.getValue()).isEqualTo("bytes");
    }

    @Test
    public void pathPresentPrefersThatValue() throws URISyntaxException {

        final URI uri = ClassLoader.getSystemResource("keyfile.txt").toURI();

        final PrivateKey privateKey = new PrivateKey(null, Paths.get(uri), null, null, null, null, null, null, null);

        assertThat(privateKey.getValue()).isEqualTo("SOMEDATA");
    }

    @Test
    public void nullPathAndLegacyGivesDefaultValue() {

        final PrivateKey privateKey = new PrivateKey(null, null, "DATA", null, null, null, null, null, null);

        assertThat(privateKey.getValue()).isEqualTo("DATA");

    }

}
