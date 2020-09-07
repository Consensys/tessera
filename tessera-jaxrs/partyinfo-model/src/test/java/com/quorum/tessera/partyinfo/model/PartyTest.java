package com.quorum.tessera.partyinfo.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyTest {

    @Test
    public void toStringContainsUrl() {
        final Party party = new Party("someurl");

        assertThat(party.toString()).contains("someurl");

    }

    @Test
    public void urlIsNormalized() {
        final Party party = new Party("http://someurl.com");
        assertThat(party.getUrl()).isEqualTo("http://someurl.com/");
    }

    @Test
    public void hashCodeAndEquals() {
        EqualsVerifier.forClass(Party.class).verify();
    }

}
