package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.ClientMode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientModeAdapterTest {

    private ClientModeAdapter adapter = new ClientModeAdapter();

    @Test
    public void testMarshal() {
        assertThat(adapter.marshal(ClientMode.ORION)).isEqualTo("orion");
        assertThat(adapter.marshal(ClientMode.TESSERA)).isEqualTo("tessera");
    }

    @Test
    public void testUnmarshal() {
        assertThat(adapter.unmarshal("ORION")).isEqualTo(ClientMode.ORION);
        assertThat(adapter.unmarshal("TESSERA")).isEqualTo(ClientMode.TESSERA);
        assertThat(adapter.unmarshal("orion")).isEqualTo(ClientMode.ORION);
        assertThat(adapter.unmarshal("TesserA")).isEqualTo(ClientMode.TESSERA);

        // Return default mode
        assertThat(adapter.unmarshal("BOGUS")).isEqualTo(ClientMode.TESSERA);
    }
}
