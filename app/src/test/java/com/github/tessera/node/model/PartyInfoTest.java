package com.github.tessera.node.model;

import com.github.tessera.nacl.Key;
import org.junit.Test;

import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PartyInfoTest {

    private static final String uri = "http://localhost:8080";

    @Test
    public void testDeepCopy() {

        final PartyInfo partyInfo = new PartyInfo(
            uri,
            Stream.of(
                new Recipient(new Key("some-key".getBytes()), uri),
                new Recipient(new Key("another-public-key".getBytes()), uri)
            ).collect(toSet()),
            emptySet()
        );

        final PartyInfo partyInfoCopy = new PartyInfo(partyInfo);

        assertThat(partyInfoCopy.getParties()).isNotSameAs(partyInfo.getParties());
        assertThat(partyInfoCopy.getRecipients()).isNotSameAs(partyInfo.getRecipients());

    }
}
