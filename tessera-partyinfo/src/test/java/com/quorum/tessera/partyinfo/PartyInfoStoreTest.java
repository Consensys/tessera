package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PartyInfoStoreTest {

    private String uri = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    @Before
    public void onSetUp() throws URISyntaxException {
        this.partyInfoStore = new PartyInfoStore(new URI(uri));
    }

    @Test
    public void ourUrlEndsWithSlash() {
        final PartyInfo stored = this.partyInfoStore.getPartyInfo();

        assertThat(stored.getUrl()).isEqualTo(uri + "/");
    }

    @Test
    public void registeringDifferentPeersAdds() {
        final PartyInfo incomingInfo =
                new PartyInfo("http://localhost:8080/", emptySet(), singleton(new Party("example.com/")));

        this.partyInfoStore.store(incomingInfo);

        final PartyInfo output = this.partyInfoStore.getPartyInfo();

        assertThat(output.getParties())
                .containsExactlyInAnyOrder(new Party("http://localhost:8080/"), new Party("example.com/"));
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

        final PartyInfo incomingLocal = new PartyInfo(uri, singleton(new Recipient(localKey, uri)), emptySet());
        final PartyInfo incomingRemote =
                new PartyInfo(uri, singleton(new Recipient(remoteKey, "example.com")), emptySet());

        partyInfoStore.store(incomingLocal);
        partyInfoStore.store(incomingRemote);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(new Recipient(localKey, uri), new Recipient(remoteKey, "example.com"));
    }

    @Test
    public void registeringSamePublicKeyTwiceDoesntAdd() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(new Recipient(testKey, uri));

        final PartyInfo incoming = new PartyInfo(uri, ourKeys, emptySet());

        partyInfoStore.store(incoming);
        partyInfoStore.store(incoming);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(new Recipient(testKey, uri));
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

        final Set<Recipient> ourKeys = singleton(new Recipient(testKey, uri));

        final PartyInfo initial = new PartyInfo(uri, ourKeys, emptySet());

        partyInfoStore.store(initial);

        final Set<Recipient> newRecipients = singleton(new Recipient(testKey, "http://other.com"));
        final PartyInfo updated = new PartyInfo(uri, newRecipients, emptySet());

        partyInfoStore.store(updated);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients).hasSize(1).containsExactly(new Recipient(testKey, "http://other.com"));
    }

    @Test
    public void removeRecipient() {
        // Given
        final PublicKey someKey = PublicKey.from("someKey".getBytes());
        final PublicKey someOtherKey = PublicKey.from("someOtherKey".getBytes());

        final PartyInfo somePartyInfo = new PartyInfo(uri, singleton(new Recipient(someKey, uri)), emptySet());
        final PartyInfo someOtherPartyInfo =
                new PartyInfo(uri, singleton(new Recipient(someOtherKey, "somedomain.com")), emptySet());

        partyInfoStore.store(somePartyInfo);
        partyInfoStore.store(someOtherPartyInfo);

        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();

        assertThat(retrievedRecipients)
                .hasSize(2)
                .containsExactlyInAnyOrder(new Recipient(someKey, uri), new Recipient(someOtherKey, "somedomain.com"));

        // When
        PartyInfo result = partyInfoStore.removeRecipient("somedomain.com");

        assertThat(result).isNotNull();
        assertThat(result.getRecipients()).hasSize(1).containsOnly(new Recipient(someKey, uri));
    }

    @Test
    public void defaultConstructor() {
        //Yuk. Need to address in RuntimeContext.getInstance()
        RuntimeContextFactory.newFactory().create(mock(Config.class));
        assertThat(new PartyInfoStore()).isNotNull();
    }
}
