package com.quorum.tessera.partyinfo.model;

import java.util.HashMap;
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
    public void twoPartiesWithSameUrlAreEqual() {
        String url = "http://foo.com";
        Party party = new Party(url);
        Party anotherParty = new Party(url);
        assertThat(party).isEqualTo(anotherParty);
    }

    @Test
    public void nullPartyIsNotEqual() {
        String url = "http://foo.com";
        Party party = new Party(url);
        Party anotherParty = null;
        assertThat(party).isNotEqualTo(anotherParty);
    }

    @Test
    public void objectsOfDifferentTypesArentEqual() {
        String url = "http://foo.com";
        Party party = new Party(url);
        Object anotherParty = new HashMap();
        assertThat(party).isNotEqualTo(anotherParty);
    }
}
