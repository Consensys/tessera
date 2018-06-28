package com.github.nexus.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicKeyTest {

    @Test
    public void readPathDataWhenValueIsNull() throws Exception {

        final URI uri = ClassLoader.getSystemResource("keyfile.txt").toURI();

        final PublicKey publicKey = new PublicKey();
        final Field value = PublicKey.class.getDeclaredField("path");
        value.setAccessible(true);

        value.set(publicKey, Paths.get(uri));

        assertThat(publicKey.getValue()).isEqualTo("SOMEDATA");

    }


}
