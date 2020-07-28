package com.quorum.tessera.partyinfo;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExclusionCacheTest {

    @Test
    public void create() {
        ExclusionCache result = ExclusionCache.create();
        assertThat(result).isNotNull().isExactlyInstanceOf(RecipientExclusionCache.class);
    }
}
