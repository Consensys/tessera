package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class EnclaveKeySynchroniserTest {

    private static final String URL = "myurl.com/";

    private Enclave enclave;

    private PartyInfoStore partyInfoStore;

    private ConfigService configService;

    private EnclaveKeySynchroniser enclaveKeySynchroniser;

    @Before
    public void init() throws URISyntaxException {
        this.configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI(URL));

        this.enclave = mock(Enclave.class);
        this.partyInfoStore = PartyInfoStore.create(URI.create(URL));

        this.enclaveKeySynchroniser = new EnclaveKeySynchroniser(enclave, partyInfoStore, configService);
    }

    @After
    public void after() {
        PartyInfoStoreImpl.INSTANCE.clear();
        verifyNoMoreInteractions(enclave, configService);
    }

    @Test
    public void fetchedKeysAreAddedToStore() {
        final PartyInfo initialStore = this.partyInfoStore.getPartyInfo();
        assertThat(initialStore.getRecipients()).isEmpty();
        assertThat(initialStore.getParties()).containsExactlyInAnyOrder(new Party(URL));

        final PublicKey keyOne = PublicKey.from("KeyOne".getBytes());
        final PublicKey keyTwo = PublicKey.from("KeyTwo".getBytes());

        when(enclave.getPublicKeys()).thenReturn(new HashSet<>(Arrays.asList(keyOne, keyTwo)));

        this.enclaveKeySynchroniser.run();

        final PartyInfo updatedStore = this.partyInfoStore.getPartyInfo();
        assertThat(updatedStore.getRecipients())
                .containsExactlyInAnyOrder(new Recipient(keyOne, URL), new Recipient(keyTwo, URL));

        verify(enclave).getPublicKeys();
        verify(configService).getServerUri();
    }

    @Test
    public void connectionIssuesBubbleUp() {
        when(enclave.getPublicKeys()).thenThrow(UncheckedIOException.class);

        final Throwable throwable = catchThrowable(this.enclaveKeySynchroniser::run);
        assertThat(throwable).isInstanceOf(UncheckedIOException.class);

        final PartyInfo store = this.partyInfoStore.getPartyInfo();
        assertThat(store.getRecipients()).isEmpty();
        assertThat(store.getParties()).containsExactlyInAnyOrder(new Party(URL));

        verify(enclave).getPublicKeys();
        verify(configService).getServerUri();
    }
}
