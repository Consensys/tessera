package com.github.nexus.config.jaxb;

import com.github.nexus.config.PrivateKeyType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrivateKeyTypeAdapterTest {

    private PrivateKeyTypeAdapter privateKeyTypeAdapter;

    public PrivateKeyTypeAdapterTest() {
    }

    @Before
    public void setUp() {
        privateKeyTypeAdapter = new PrivateKeyTypeAdapter();
    }

    @After
    public void tearDown() {
        privateKeyTypeAdapter = null;
    }

    @Test
    public void marshalLocked() throws Exception {

        String result = privateKeyTypeAdapter.marshal(PrivateKeyType.LOCKED);

        assertThat(result).isEqualTo("locked");
    }

    @Test
    public void marshalUnlocked() throws Exception {

        String result = privateKeyTypeAdapter.marshal(PrivateKeyType.UNLOCKED);

        assertThat(result).isEqualTo("unlocked");
    }

    @Test
    public void unmarshalLocked() throws Exception {

        PrivateKeyType result = privateKeyTypeAdapter.unmarshal("locked");

        assertThat(result).isEqualTo(PrivateKeyType.LOCKED);
    }

    @Test
    public void unmarshalUnlocked() throws Exception {

        PrivateKeyType result = privateKeyTypeAdapter.unmarshal("unlocked");

        assertThat(result).isEqualTo(PrivateKeyType.UNLOCKED);
    }
}
