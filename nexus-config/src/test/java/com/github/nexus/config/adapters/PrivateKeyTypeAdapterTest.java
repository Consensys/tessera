package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKeyType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrivateKeyTypeAdapterTest {

    private PrivateKeyTypeAdapter adapter;

    public PrivateKeyTypeAdapterTest() {
    }

    @Before
    public void setUp() {
        adapter = new PrivateKeyTypeAdapter();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void marshalLocked() throws Exception {
        assertThat(adapter.marshal(PrivateKeyType.LOCKED)).isEqualTo("argon2sbox");
    }

    @Test
    public void marshalUnlocked() throws Exception {
        assertThat(adapter.marshal(PrivateKeyType.UNLOCKED)).isEqualTo("unlocked");
    }

    @Test
    public void marshalNull() throws Exception {
        assertThat(adapter.marshal(null)).isNull();
    }

    @Test
    public void unmarshalLocked() throws Exception {
        assertThat(adapter.unmarshal("argon2sbox")).isEqualTo(PrivateKeyType.LOCKED);
    }

    @Test
    public void unmarshalUnlocked() throws Exception {
        assertThat(adapter.unmarshal("unlocked")).isEqualTo(PrivateKeyType.UNLOCKED);
    }

    @Test
    public void unmarshalNull() throws Exception {
        assertThat(adapter.unmarshal(null)).isNull();
    }
}
