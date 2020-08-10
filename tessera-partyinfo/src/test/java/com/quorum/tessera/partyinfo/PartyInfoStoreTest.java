package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.partyinfo.node.VersionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyInfoStoreTest {

    private String uri = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    @Before
    public void onSetUp() {
        this.partyInfoStore = new PartyInfoStoreImpl(URI.create(uri));
    }

    @After
    public void onTearDown() {}

    @Test
    public void ourUrlEndsWithSlash() {
        final NodeInfo stored = this.partyInfoStore.getPartyInfo();

        assertThat(stored.getUrl()).isEqualTo(uri + "/");
    }

    @Test
    public void registeringDifferentPeersAdds() {
        final String url = "http://localhost:8080/";

        final NodeInfo incomingInfo = NodeInfo.Builder.create()
            .withUrl(url)
            .withParties(Set.of(new Party("example.com/")))
            .build();

        this.partyInfoStore.store(incomingInfo);

        final NodeInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties())
                .containsExactlyInAnyOrder(new Party(url), new Party("example.com/"));
    }

    @Test
    public void registeringSamePeerTwiceDoesntAdd() {
        final String url = "http://localhost:8080/";

        final NodeInfo incomingInfo = NodeInfo.Builder.create()
                .withUrl(url)
                .withParties(Set.of(new Party(url)))
                .build();

        this.partyInfoStore.store(incomingInfo);

        final NodeInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties()).containsExactlyInAnyOrder(new Party(url));
    }

    @Test
    public void registeringDifferentPublicKeyAdds() {
        final PublicKey localKey = PublicKey.from("local-key".getBytes());
        final PublicKey remoteKey = PublicKey.from("remote-key".getBytes());

        final NodeInfo incomingLocal = NodeInfo.Builder.create()
            .withRecipients(Set.of(Recipient.of(localKey, uri)))
            .withUrl(uri)
            .build();

        final NodeInfo incomingRemote = NodeInfo.Builder.create()
            .withUrl(uri).withRecipients(Set.of(Recipient.of(remoteKey, "example.com")))
            .build();

        partyInfoStore.store(incomingLocal);
        partyInfoStore.store(incomingRemote);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(Recipient.of(localKey, uri), Recipient.of(remoteKey, "example.com"));
    }

    @Test
    public void registeringSamePublicKeyTwiceDoesntAdd() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(Recipient.of(testKey, uri));

        final NodeInfo incoming = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(ourKeys)
            .build();

        partyInfoStore.store(incoming);
        partyInfoStore.store(incoming);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(Recipient.of(testKey, uri));
    }

    @Test
    public void receivingMessageUpdatesSenderTimestamp() {

        final String ourUpdatedUri = uri + "/";

        final NodeInfo incoming = NodeInfo.Builder.create()
            .withUrl(ourUpdatedUri)
            .build();

        partyInfoStore.store(incoming);

        final NodeInfo afterFirst = this.partyInfoStore.getPartyInfo();

        partyInfoStore.store(incoming);

        final NodeInfo afterSecond = this.partyInfoStore.getPartyInfo();

        assertThat(afterFirst.getParties()).hasSize(1);
        assertThat(afterSecond.getParties()).hasSize(1);

        final Instant firstContact = afterFirst.getParties().iterator().next().getLastContacted();
        final Instant secondContact = afterSecond.getParties().iterator().next().getLastContacted();

        // test can run to quickly, so the time may be the same (hence before or equal)
        // so check they are not the same object, meaning the time was overwritten (just with the same time)
        assertThat(firstContact).isNotSameAs(secondContact);
        assertThat(firstContact).isBeforeOrEqualTo(secondContact);
    }

    @Test
    public void attemptToUpdateRecipientWithExistingKeyWithNewUrlIsUpdated() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(Recipient.of(testKey, uri));

        final NodeInfo initial = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(ourKeys)
            .build();

        partyInfoStore.store(initial);

        final Set<Recipient> newRecipients = singleton(Recipient.of(testKey, "http://other.com"));
        final NodeInfo updated = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(newRecipients)
            .build();

        partyInfoStore.store(updated);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(Recipient.of(testKey, "http://other.com"));
    }

    @Test
    public void removeRecipient() {
        // Given
        final PublicKey someKey = PublicKey.from("someKey".getBytes());
        final PublicKey someOtherKey = PublicKey.from("someOtherKey".getBytes());

        final NodeInfo somePartyInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(Set.of(Recipient.of(someKey, uri)))
            .build();

        final NodeInfo someOtherPartyInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(Set.of(Recipient.of(someOtherKey, "somedomain.com")))
            .build();

        partyInfoStore.store(somePartyInfo);
        partyInfoStore.store(someOtherPartyInfo);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(Recipient.of(someKey, uri), Recipient.of(someOtherKey, "somedomain.com"));

        // When
        NodeInfo result = partyInfoStore.removeRecipient("somedomain.com");

        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(1).containsOnly(Recipient.of(someKey, uri));
    }

    @Test
    public void findRecipientByPublicKey() {

        PublicKey myKey = PublicKey.from("I LOVE SPARROWS".getBytes());
        Recipient recipient = Recipient.of(myKey, "http://myurl.com");

        NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(Set.of(recipient))
            .build();

        partyInfoStore.store(partyInfo);

        Recipient result = partyInfoStore.findRecipientByPublicKey(myKey);
        assertThat(result).isSameAs(recipient);
    }

    @Test(expected = KeyNotFoundException.class)
    public void findRecipientByPublicKeyNoKeyFound() {

        PublicKey myKey = PublicKey.from("I LOVE SPARROWS".getBytes());
        Recipient recipient = Recipient.of(myKey, "http://myurl.com");

        NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(Set.of(recipient))
            .build();

        partyInfoStore.store(partyInfo);

        partyInfoStore.findRecipientByPublicKey(PublicKey.from("OTHER KEY".getBytes()));
    }

    @Test
    public void getAdvertisedUrl() {
        assertThat(partyInfoStore.getAdvertisedUrl()).startsWith(uri).endsWith("/");
    }

    @Test
    public void create() {

        RuntimeContextFactory.newFactory().create(null);
        PartyInfoStore instance = PartyInfoStore.create(URI.create("http://junit.com"));
        assertThat(instance).isNotNull();
    }

    @Test
    public void storePartyInfoWithVersion() {

        final VersionInfo versionInfo = mock(VersionInfo.class);
        when(versionInfo.supportedApiVersions()).thenReturn(Set.of("v1","v2"));

        final String url = "http://localhost:8080/";

        final Set<String> supportedApiVersions = Set.of("v1","v2");

        final NodeInfo incomingInfo = NodeInfo.Builder.create()
            .withUrl(url)
            .withParties(Set.of(new Party("example.com/")))
            .withSupportedApiVersions(supportedApiVersions)
            .build();

        this.partyInfoStore.store(incomingInfo);

        final NodeInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties())
            .containsExactlyInAnyOrder(new Party(url), new Party("example.com/"));

        final VersionInfo version = this.partyInfoStore.getVersionInfo(new Party(url));
        assertThat(version.supportedApiVersions()).isEqualTo(supportedApiVersions);

    }
}
