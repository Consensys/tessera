package com.quorum.tessera.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliTypeTest {

    @Test
    public void values() {
        for(CliType t : CliType.values()) {
            assertThat(t).isNotNull();
            assertThat(CliType.valueOf(t.name())).isSameAs(t);
        }
    }

}
