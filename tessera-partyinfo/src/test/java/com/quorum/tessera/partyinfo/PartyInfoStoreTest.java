package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyInfoStoreTest {

    private String uri = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    private ExclusionCache exclusionCache;

    @Before
    public void onSetUp() throws URISyntaxException {
        exclusionCache = mock(ExclusionCache.class);
        this.partyInfoStore = new PartyInfoStoreImpl(URI.create(uri),exclusionCache);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(exclusionCache);

    }

    @Test
    public void ourUrlEndsWithSlash() {
        final PartyInfo stored = this.partyInfoStore.getPartyInfo();

        assertThat(stored.getUrl()).isEqualTo(uri + "/");
    }

    @Test
    public void registeringDifferentPeersAdds() {
        String targetUrl = "example.com/";

        Recipient recipient = Recipient.of(PublicKey.from("SomeData".getBytes()),targetUrl);

        final PartyInfo incomingInfo =
                new PartyInfo("http://localhost:8080/", Set.of(recipient), Set.of((new Party(targetUrl))));

        this.partyInfoStore.store(incomingInfo);

        final PartyInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties())
                .containsExactlyInAnyOrder(new Party("http://localhost:8080/"), new Party(targetUrl));

        verify(exclusionCache).isExcluded(recipient);
    }

    @Test
    public void registeringSamePeerTwiceDoesntAdd() {
        final PartyInfo incomingInfo =
                new PartyInfo("http://localhost:8080/", emptySet(), singleton(new Party("http://localhost:8080/")));

        this.partyInfoStore.store(incomingInfo);

        final PartyInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties()).containsExactlyInAnyOrder(new Party("http://localhost:8080/"));
    }

    @Test
    public void registeringDifferentPublicKeyAdds() {
        final PublicKey localKey = PublicKey.from("local-key".getBytes());
        final PublicKey remoteKey = PublicKey.from("remote-key".getBytes());

        final PartyInfo incomingLocal = new PartyInfo(uri, singleton(Recipient.of(localKey, uri)), emptySet());
        final PartyInfo incomingRemote =
                new PartyInfo(uri, singleton(Recipient.of(remoteKey, "example.com")), emptySet());

        partyInfoStore.store(incomingLocal);
        partyInfoStore.store(incomingRemote);

        verify(exclusionCache,times(2)).isExcluded(any(Recipient.class));

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(Recipient.of(localKey, uri), Recipient.of(remoteKey, "example.com"));
    }

    @Test
    public void registeringSamePublicKeyTwiceDoesntAdd() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(Recipient.of(testKey, uri));

        final PartyInfo incoming = new PartyInfo(uri, ourKeys, emptySet());

        partyInfoStore.store(incoming);
        partyInfoStore.store(incoming);

        verify(exclusionCache,times(2)).isExcluded(any(Recipient.class));


        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(Recipient.of(testKey, uri));
    }

    @Test
    public void receivingMessageUpdatesSenderTimestamp() {

        final String ourUpdatedUri = uri + "/";

        final PartyInfo incoming = new PartyInfo(ourUpdatedUri, emptySet(), emptySet());

        partyInfoStore.store(incoming);

        final PartyInfo afterFirst = this.partyInfoStore.getPartyInfo();

        partyInfoStore.store(incoming);

        final PartyInfo afterSecond = this.partyInfoStore.getPartyInfo();

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
    public void attemptToUpdateReciepentWithExistingKeyWithNewUrlIsUpdated() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(Recipient.of(testKey, uri));

        final PartyInfo initial = new PartyInfo(uri, ourKeys, emptySet());

        partyInfoStore.store(initial);

        final Set<Recipient> newRecipients = singleton(Recipient.of(testKey, "http://other.com"));
        final PartyInfo updated = new PartyInfo(uri, newRecipients, emptySet());

        partyInfoStore.store(updated);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(Recipient.of(testKey, "http://other.com"));

        verify(exclusionCache,times(2)).isExcluded(any(Recipient.class));

    }

    @Test
    public void removeRecipient() {
        // Given
        final PublicKey someKey = PublicKey.from("someKey".getBytes());
        final PublicKey someOtherKey = PublicKey.from("someOtherKey".getBytes());

        final PartyInfo somePartyInfo = new PartyInfo(uri, singleton(Recipient.of(someKey, uri)), emptySet());
        final PartyInfo someOtherPartyInfo =
                new PartyInfo(uri, singleton(Recipient.of(someOtherKey, "somedomain.com")), emptySet());

        partyInfoStore.store(somePartyInfo);
        partyInfoStore.store(someOtherPartyInfo);

        verify(exclusionCache,times(2)).isExcluded(any(Recipient.class));

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(Recipient.of(someKey, uri), Recipient.of(someOtherKey, "somedomain.com"));

        // When
        PartyInfo result = partyInfoStore.removeRecipient("somedomain.com");

        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(1).containsOnly(Recipient.of(someKey, uri));
        verify(exclusionCache).exclude(any(Recipient.class));

    }

    @Test
    public void findRecipientByPublicKey() {

        PublicKey myKey = PublicKey.from("I LOVE SPARROWS".getBytes());
        Recipient recipient = Recipient.of(myKey, "http://myurl.com");

        PartyInfo partyInfo = new PartyInfo(uri, singleton(recipient), Collections.EMPTY_SET);
        partyInfoStore.store(partyInfo);

        Recipient result = partyInfoStore.findRecipientByPublicKey(myKey);
        assertThat(result).isSameAs(recipient);

        verify(exclusionCache).isExcluded(any(Recipient.class));

    }

    @Test(expected = KeyNotFoundException.class)
    public void findRecipientByPublicKeyNoKeyFound() {

        PublicKey myKey = PublicKey.from("I LOVE SPARROWS".getBytes());
        Recipient recipient = Recipient.of(myKey, "http://myurl.com");

        PartyInfo partyInfo = new PartyInfo(uri, singleton(recipient), Collections.EMPTY_SET);
        partyInfoStore.store(partyInfo);

        verify(exclusionCache).isExcluded(any(Recipient.class));

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
    public void recipientInExcludeCacheIdFiltered() {

        String targetUrl = "http://jonnysixkiller.com/";

        Recipient recipient = Recipient.of(PublicKey.from("SomeData".getBytes()),targetUrl);

        final PartyInfo initialPartyInfo =
            new PartyInfo("http://localhost:8080/",Set.of(recipient),
                Set.of(new Party(targetUrl)));

        this.partyInfoStore.store(initialPartyInfo);
        partyInfoStore.removeRecipient(targetUrl);
        verify(exclusionCache).exclude(recipient);

        when(exclusionCache.isExcluded(recipient)).thenReturn(true);

        final PartyInfo incomingInfo =
            new PartyInfo("http://localhost:8080/",Set.of(recipient), Set.of(new Party(targetUrl)));

        this.partyInfoStore.store(incomingInfo);

        final PartyInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties())
            .containsOnly(new Party("http://localhost:8080/"));

        assertThat(output.getRecipients()).isEmpty();

        verify(exclusionCache,times(2)).isExcluded(recipient);

    }
}
