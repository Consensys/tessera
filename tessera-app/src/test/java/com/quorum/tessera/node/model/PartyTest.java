package com.quorum.tessera.node.model;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyTest {

    @Test
    public void differentClassesAreNotEqual() {

        final Object other =  "test";
        final boolean isEqual = Objects.equals(new Party("partyurl"), other);

        assertThat(isEqual).isFalse();
    }

    @Test
    public void sameInstanceIsEqual() {
        final Party party = new Party("partyurl");

        assertThat(party).isEqualTo(party).isSameAs(party);
    }

    @Test
    public void getKeyBytes() {
        final Party party = new Party("partyurl");

        assertThat(party.getUrl())
            .isEqualTo("partyurl")
            .isSameAs("partyurl");
    }

    @Test
    public void hashCodeIsSame() {

        final Party party = new Party("partyurl");

        assertThat(party).hasSameHashCodeAs(new Party("partyurl"));
    }

}
