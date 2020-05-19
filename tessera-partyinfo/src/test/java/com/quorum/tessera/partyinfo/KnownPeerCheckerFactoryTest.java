package com.quorum.tessera.partyinfo;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class KnownPeerCheckerFactoryTest {

    @Test
    public void create() {
        KnownPeerCheckerFactory factory = new KnownPeerCheckerFactory();
        KnownPeerChecker result = factory.create(Collections.singleton("url"));
        assertThat(result).isNotNull();
    }

}
