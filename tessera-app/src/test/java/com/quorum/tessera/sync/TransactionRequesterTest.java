package com.quorum.tessera.sync;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.PostDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionRequesterTest {

    private static final Key KEY_ONE = new Key(new byte[]{1});

    private static final Key KEY_TWO = new Key(new byte[]{2});

    private KeyManager keyManager;

    private PostDelegate postDelegate;

    private TransactionRequester transactionRequester;

    @Before
    public void init() {

        this.keyManager = mock(KeyManager.class);
        this.postDelegate = mock(PostDelegate.class);

        doReturn(true).when(postDelegate).makeResendRequest(anyString(), any(ResendRequest.class));

        this.transactionRequester = new TransactionRequesterImpl(keyManager, postDelegate);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(keyManager, postDelegate);
    }

    @Test
    public void noPublicKeysMakesNoCalls() {
        doReturn(emptySet()).when(keyManager).getPublicKeys();

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verifyZeroInteractions(postDelegate);
        verify(keyManager).getPublicKeys();
    }

    @Test
    public void multipleKeysMakesCorrectCalls() {
        final Set<Key> allKeys = new HashSet<>(asList(KEY_ONE, KEY_TWO));

        doReturn(allKeys).when(keyManager).getPublicKeys();

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl1.com");

        final ArgumentCaptor<ResendRequest> captor = ArgumentCaptor.forClass(ResendRequest.class);
        verify(postDelegate, times(2)).makeResendRequest(eq("fakeurl1.com"), captor.capture());
        verify(keyManager).getPublicKeys();

        assertThat(captor.getAllValues())
            .hasSize(2)
            .extracting("publicKey")
            .containsExactlyInAnyOrder(KEY_ONE.toString(), KEY_TWO.toString());
    }

    @Test
    public void failedCallRetries() {
        doReturn(singleton(KEY_ONE)).when(keyManager).getPublicKeys();
        doReturn(false).when(postDelegate).makeResendRequest(anyString(), any(ResendRequest.class));

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(postDelegate, times(5)).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
        verify(keyManager).getPublicKeys();

    }

    @Test
    public void calltoPostDelegateThrowsException() {
        doReturn(singleton(KEY_ONE)).when(keyManager).getPublicKeys();
        doThrow(RuntimeException.class).when(postDelegate).makeResendRequest(anyString(), any(ResendRequest.class));

        this.transactionRequester.requestAllTransactionsFromNode("fakeurl.com");

        verify(postDelegate, times(5)).makeResendRequest(eq("fakeurl.com"), any(ResendRequest.class));
        verify(keyManager).getPublicKeys();

    }
}
