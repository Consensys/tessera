package com.quorum.tessera.discovery;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkStoreProviderTest {

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new NetworkStoreProvider()).isNotNull();
    }

    @Test
    public void provider() {
        assertThat(NetworkStoreProvider.provider()).isNotNull()
            .isExactlyInstanceOf(DefaultNetworkStore.class);
    }

}
