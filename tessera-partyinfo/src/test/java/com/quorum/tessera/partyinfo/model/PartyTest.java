package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.partyinfo.model.Party;
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
}
