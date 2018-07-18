package com.github.tessera.node;

import com.github.tessera.api.model.ApiPath;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.ConnectException;
import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private static final String OWN_URL = "http://own.com:8080";

    private static final String TARGET_URL = "http://bogus.com:9878";

    private static final byte[] RESPONSE = "BOGUS".getBytes();

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser;

    private PartyInfoPoller partyInfoPoller;

    private TransactionRequester transactionRequester;

    private PostDelegate postDelegate;

    @Before
    public void setUp() {
        this.postDelegate = mock(PostDelegate.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.transactionRequester = mock(TransactionRequester.class);

        this.partyInfoPoller = new PartyInfoPoller(
            partyInfoService, partyInfoParser, postDelegate, transactionRequester
        );
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, transactionRequester);
    }

    @Test
    public void run() {

        doReturn(RESPONSE).when(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();

        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        doReturn(singleton(new Party(TARGET_URL))).when(partyInfoService).findUnsavedParties(any(PartyInfo.class));

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoService).findUnsavedParties(updatedPartyInfo);

        verify(partyInfoParser).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);

        verify(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);

        //all nodes were known about, so this is called with no new URLs
        final ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(transactionRequester).requestAllTransactionsFromNode(captor.capture());
        assertThat(captor.getValue()).hasSize(1).containsExactlyInAnyOrder("http://bogus.com:9878");
    }

    @Test
    public void testWhenURLIsOwn() {

        doReturn(RESPONSE).when(postDelegate).doPost(OWN_URL, ApiPath.PARTYINFO, RESPONSE);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(OWN_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void testWhenPostFails() {

        doReturn(null).when(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
        verify(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);
    }

    @Test
    public void runThrowsException() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        doThrow(UnsupportedOperationException.class).when(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService, never()).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
    }

    @Test
    public void runThrowsConnectionExceptionAndDoesNotThrow() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        final RuntimeException connectionException = new RuntimeException(new ConnectException("OUCH"));
        doThrow(connectionException).when(postDelegate).doPost(TARGET_URL, ApiPath.PARTYINFO, RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService, never()).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
    }

}
