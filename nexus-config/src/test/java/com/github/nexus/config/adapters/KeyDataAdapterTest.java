package com.github.nexus.config.adapters;

import com.github.nexus.config.KeyData;
import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.KeyDataConfigStore;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class KeyDataAdapterTest {

    private KeyDataAdapter keyDataAdapter;

    private final KeyDataConfigStore keyDataConfigStore = KeyDataConfigStore.INSTANCE;

    private KeyDataConfig keyDataConfig;

    public KeyDataAdapterTest() {
    }

    @Before
    public void setUp() {
        keyDataConfig = mock(KeyDataConfig.class);

        keyDataConfigStore.push(keyDataConfig);
        keyDataAdapter = new KeyDataAdapter();
    }

    @After
    public void tearDown() {
        keyDataConfigStore.clear();
    }

    @Test
    public void unmarshal() throws Exception {

        KeyData keyData = new KeyData(keyDataConfig, null, null);

        KeyData result = keyDataAdapter.unmarshal(keyData);

        assertThat(result).isNotNull();
        assertThat(result).isNotSameAs(keyData);
        assertThat(result.getConfig()).isSameAs(keyDataConfig);
        assertThat(keyDataConfigStore.isEmpty()).isTrue();
    }

    @Test
    public void unmarshalWithKeys() throws Exception {

        KeyData keyData = new KeyData(keyDataConfig, "SOMEVALUE", "SOMEOTHERVALUE");

        KeyData result = keyDataAdapter.unmarshal(keyData);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(keyData);
        assertThat(result.getConfig()).isSameAs(keyDataConfig);
        assertThat(keyDataConfigStore.isEmpty()).isFalse();
    }

    @Test
    public void marshal() throws Exception {
        KeyData keyData = mock(KeyData.class);
        KeyData result = keyDataAdapter.marshal(keyData);
        assertThat(result).isSameAs(keyData);
        assertThat(keyDataConfigStore.isEmpty()).isFalse();
    }

}
