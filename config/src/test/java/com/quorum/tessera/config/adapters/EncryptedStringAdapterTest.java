package com.quorum.tessera.config.adapters;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EncryptedStringAdapterTest {

    private EncryptedStringAdapter adapter;

    @Rule public final EnvironmentVariables envVariables = new EnvironmentVariables();

    @Before
    public void init() {

        final String filePath = getClass().getResource("/key.secret").getPath();
        envVariables.set(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH, filePath);
        adapter = new EncryptedStringAdapter();
    }

    @Test
    public void testMarshall() {

        final String expectedValue = "password";

        assertThat(adapter.marshal("password")).isEqualTo(expectedValue);

        assertThat(adapter.marshal(null)).isNull();
    }

    @Test
    public void testUnMarshall() {

        String normalPassword = "password";

        assertThat(adapter.unmarshal("password")).isEqualTo(normalPassword);

        assertThat(adapter.unmarshal("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)")).isEqualTo("password");

        assertThat(adapter.unmarshal(null)).isNull();
    }

    @Test
    public void testUnMarshallWithUserInputSecret() {

        envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);

        adapter = new EncryptedStringAdapter();

        ByteArrayInputStream in = new ByteArrayInputStream(("quorum" + System.lineSeparator() + "quorum").getBytes());
        System.setIn(in);

        assertThat(adapter.unmarshal("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)")).isEqualTo("password");
    }

    @Test
    public void testUnMarshallWrongPassword() {

        envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);

        adapter = new EncryptedStringAdapter();

        ByteArrayInputStream in = new ByteArrayInputStream(("bogus" + System.lineSeparator() + "bogus").getBytes());
        System.setIn(in);

        assertThatExceptionOfType(EncryptionOperationNotPossibleException.class)
                .isThrownBy(() -> adapter.unmarshal("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)"));
    }
}
