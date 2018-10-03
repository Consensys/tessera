package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyDataBuilderTest {

    @Test
    public void buildThreeLocked() throws IOException {

        final Path passwordFile = Files.createTempFile("tessera-passwords", ".txt");

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

        Files.write(passwordFile, Arrays.asList("SECRET1", "SECRET2", "SECRET3"));

        List<ConfigKeyPair> result = KeyDataBuilder.create()
            .withPrivateKeys(privateKeys)
            .withPublicKeys(publicKeys)
            .withPrivateKeyPasswordFile(passwordFile.toString())
            .build()
            .getKeyData();

        assertThat(result).hasSize(3);

    }

    @Test
    public void differentAmountOfKeysThrowsError() {

        final KeyDataBuilder keyDataBuilder = KeyDataBuilder.create()
            .withPrivateKeys(Collections.emptyList())
            .withPublicKeys(Collections.singletonList("keyfile.txt"))
            .withPrivateKeyPasswordFile("pwfile.txt");

        final Throwable throwable = catchThrowable(() -> keyDataBuilder.build());

        assertThat(throwable)
            .isInstanceOf(ConfigException.class)
            .hasCauseExactlyInstanceOf(RuntimeException.class);

        assertThat(throwable.getCause()).hasMessage("Different amount of public and private keys supplied");

    }
    
    @Test
    public void buildThreeLockedPasswordsFile() throws IOException {

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
        
        Path passwordsFile = Files.createTempFile("buildThreeLockedPasswordsFile", ".txt");
        Files.write(passwordsFile, privateKeyPasswords);
        
        
        List<ConfigKeyPair> result = KeyDataBuilder.create()
                .withPrivateKeys(privateKeys)
                .withPublicKeys(publicKeys)
                .withPrivateKeyPasswordFile(passwordsFile.toString())
                .build().getKeyData();

        assertThat(result).hasSize(3);

//        assertThat(result.get(0).getConfig().getPassword()).isEqualTo("SECRET1");
//        assertThat(result.get(1).getConfig().getPassword()).isEqualTo("SECRET2");
//        assertThat(result.get(2).getConfig().getPassword()).isEqualTo("SECRET3");

//        assertThat(result.get(0).getPublicKey()).isEqualTo("PUB1");
//        assertThat(result.get(1).getPublicKey()).isEqualTo("PUB2");
//        assertThat(result.get(2).getPublicKey()).isEqualTo("PUB3");

        for (Path p : privateKeyPaths) {
            Files.deleteIfExists(p);
        }

    }

    @Test
    public void noKeysReturnsEmptyList() {
        KeyConfiguration keyConfiguration = KeyDataBuilder.create().build();
        assertThat(keyConfiguration.getKeyData()).isEmpty();
    }
}
