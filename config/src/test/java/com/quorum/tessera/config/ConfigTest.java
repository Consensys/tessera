package com.quorum.tessera.config;

import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void createWithNullArgs() {
        Config config = new Config(null, null, null, null, null, null, false, false);
        assertThat(config).isNotNull();
    }

    @Test
    public void addPeer() {
        Config config = new Config(null, null, new ArrayList<>(), null, null, null, false, false);
        assertThat(config.getPeers()).isEmpty();
        Peer peer = new Peer("Junit");
        config.addPeer(peer);
        assertThat(config.getPeers()).containsOnly(peer);

    }

}
