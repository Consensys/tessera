package com.quorum.tessera.partyinfo;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class KnownPeerCheckerTest {
    private KnownPeerChecker knownPeerChecker;
    private Set<String> peers;

    @Before
    public void setUp() {
        peers = mock(Set.class);
        knownPeerChecker = new KnownPeerChecker(peers);
    }

    @Test
    public void falseIfNullPeers() {
        final KnownPeerChecker knownPeerChecker = new KnownPeerChecker(null);
        final boolean result = knownPeerChecker.isKnown("anyurl");
        assertThat(result).isFalse();
    }

    @Test
    public void falseIfEmptyPeers() {
        final KnownPeerChecker knownPeerChecker = new KnownPeerChecker(Collections.emptySet());
        final boolean result = knownPeerChecker.isKnown("anyurl");
        assertThat(result).isFalse();
    }

    @Test
    public void trueIfCheckedUrlExactlyEqualsPeer() {
        Set<String> p = Set.of("http://url:8080", "http://another:8081");
        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://url:8080");
        assertThat(result).isTrue();
    }

    @Test
    public void falseIfDifferentUrls() {
        Set<String> p = Set.of("http://url:8080", "http://another:8081");

        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://localhost:8080");
        assertThat(result).isFalse();
    }

    @Test
    public void trueIfCheckedUrlHasTrailingSlash() {
        Set<String> p = Collections.singleton("http://url:8080");
        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://url:8080/");
        assertThat(result).isTrue();
    }

    @Test
    public void falseIfPeerUrlHasTrailingSlash() {
        Set<String> p = Collections.singleton("http://url:8080/");
        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://url:8080");
        assertThat(result).isFalse();
    }

    @Test
    public void trueIfPeerUrlIsLocalhostAndCheckedUrlIsLoopbackIP() {
        Set<String> p = Collections.singleton("http://localhost:8080");
        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://127.0.0.1:8080/");
        assertThat(result).isTrue();
    }

    @Test
    public void trueIfPeerUrlIsLoopbackIPAndCheckedUrlIsLocalhost() {
        Set<String> p = Collections.singleton("http://127.0.0.1:8080");
        when(peers.iterator()).thenReturn(p.iterator());

        final boolean result = knownPeerChecker.isKnown("http://localhost:8080/");
        assertThat(result).isTrue();
    }

    @Test
    public void runtimeExceptionIfUrlParseError() {
        Set<String> p = Collections.singleton("http://url:8080");
        when(peers.iterator()).thenReturn(p.iterator());

        final Throwable ex = catchThrowable(() -> knownPeerChecker.isKnown("malformed@url"));
        assertThat(ex).isExactlyInstanceOf(RuntimeException.class);
        assertThat(ex).hasMessageContaining("unable to check if malformed@url is a known peer");
        assertThat(ex).hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    public void runtimeExceptionIfUrlNormalizationError() {
        Set<String> p = Collections.singleton("http://url:8080");
        when(peers.iterator()).thenReturn(p.iterator());

        final Throwable ex = catchThrowable(() -> knownPeerChecker.isKnown("<><```invaliduri"));
        assertThat(ex).isExactlyInstanceOf(RuntimeException.class);
        assertThat(ex).hasMessageContaining("unable to check if <><```invaliduri is a known peer");
        assertThat(ex).hasCauseInstanceOf(RuntimeException.class);
    }
}
