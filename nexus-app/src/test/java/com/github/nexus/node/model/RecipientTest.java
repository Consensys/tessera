package com.github.nexus.node.model;

import com.github.nexus.nacl.Key;
import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipientTest {

    private static final Key TEST_KEY = new Key(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

    @Test
    public void differentClassesAreNotEqual() {
        final boolean isEqual = Objects.equals(new Recipient(TEST_KEY, "url"), "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void sameInstanceIsEqual() {
        final Recipient recipient = new Recipient(TEST_KEY, "url");

        assertThat(recipient).isEqualTo(recipient).isSameAs(recipient);
    }

//    @Test
//    public void getters() {
//        final Party party = new Party("partyurl");
//
//        assertThat(party.getUrl())
//            .isEqualTo("partyurl")
//            .isSameAs("partyurl");
//
//    }

    @Test
    public void hashCodeIsSame() {
        final Recipient recipient = new Recipient(TEST_KEY, "url");

        assertThat(recipient)
            .hasSameHashCodeAs(new Recipient(new Key(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}), "url"));

    }

}
