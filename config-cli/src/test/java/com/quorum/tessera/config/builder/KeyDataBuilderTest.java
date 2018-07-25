package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.test.FixtureUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class KeyDataBuilderTest {

    @Test
    public void buildThreeLocked() throws IOException {

        List<Path> privateKeyPaths = Arrays.asList(
                Files.createTempFile("buildThreeLocked1", ".txt"),
                Files.createTempFile("buildThreeLocked2", ".txt"),
                Files.createTempFile("buildThreeLocked3", ".txt")
        );

        
        final byte[] privateKeyData = FixtureUtil.createLockedPrivateKey().toString().getBytes();
        for (Path p : privateKeyPaths) {
            Files.write(p, privateKeyData);
        }

        List<String> publicKeys = Arrays.asList("PUB1", "PUB2", "PUB3");
        List<String> privateKeys = privateKeyPaths.stream().map(Path::toString).collect(Collectors.toList());
        List<String> privateKeyPasswords = Arrays.asList("SECRET1", "SECRET2", "SECRET3");

        List<KeyData> result = KeyDataBuilder.create()
                .withPrivateKeys(privateKeys)
                .withPublicKeys(publicKeys)
                .withPrivateKeyPasswords(privateKeyPasswords)
                .build();

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getConfig().getPassword()).isEqualTo("SECRET1");
        assertThat(result.get(1).getConfig().getPassword()).isEqualTo("SECRET2");
        assertThat(result.get(2).getConfig().getPassword()).isEqualTo("SECRET3");

        assertThat(result.get(0).getPublicKey()).isEqualTo("PUB1");
        assertThat(result.get(1).getPublicKey()).isEqualTo("PUB2");
        assertThat(result.get(2).getPublicKey()).isEqualTo("PUB3");

        for (Path p : privateKeyPaths) {
            Files.deleteIfExists(p);
        }

    }

}
