package com.jpmorgan.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.net.URI;
import java.util.Collections;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PartyInfoClientEndpointTest {

  private PartyInfoClientEndpoint partyInfoClientEndpoint;

  private WebSocketContainer container;

  private PartyInfoService partyInfoService;

  @Before
  public void onSetUp() {
    partyInfoService = mock(PartyInfoService.class);
    partyInfoClientEndpoint = new PartyInfoClientEndpoint(partyInfoService);
    container = MockContainerProvider.getInstance();
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(partyInfoService, container);
  }

  @Test
  public void onOpen() {
    Session session = mock(Session.class);
    partyInfoClientEndpoint.onOpen(session);
  }

  @Test
  public void onMessage() throws Exception {

    PartyInfo existingPartyInfo = mock(PartyInfo.class);
    when(partyInfoService.getPartyInfo()).thenReturn(existingPartyInfo);

    Session session = mock(Session.class);
    Basic basic = mock(Basic.class);
    when(session.getBasicRemote()).thenReturn(basic);
    when(session.getRequestURI()).thenReturn(URI.create("ws://foo.com"));
    PartyInfo partyInfo = mock(PartyInfo.class);

    PublicKey someKey = PublicKey.from("SOMEDATA".getBytes());
    String url = "ws://somedomain.com/someaddress";
    Recipient recipient = new Recipient(someKey, url);
    when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

    when(container.connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class))).thenReturn(session);

    partyInfoClientEndpoint.onMessage(session, partyInfo);

    verify(partyInfoService).getPartyInfo();
    verify(container).connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class));
  }

  @Test
  public void onClose() {
    Session session = mock(Session.class);
    CloseReason reason = new CloseReason(CloseCodes.CANNOT_ACCEPT, "WHAT YOU TALKIN' ABOUT WILLIS?");
    partyInfoClientEndpoint.onClose(session, reason);
  }

  @Test
  public void onMessageNodeAlreadyHasRecipient() throws Exception {

    PublicKey someKey = PublicKey.from("SOMEDATA".getBytes());
    String url = "ws://somedomain.com/someaddress";
    Recipient recipient = new Recipient(someKey, url);

    PartyInfo existingPartyInfo = mock(PartyInfo.class);
    when(existingPartyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

    when(partyInfoService.getPartyInfo()).thenReturn(existingPartyInfo);

    Session session = mock(Session.class);
    Basic basic = mock(Basic.class);
    when(session.getBasicRemote()).thenReturn(basic);
    when(session.getRequestURI()).thenReturn(URI.create("ws://foo.com"));

    PartyInfo partyInfo = mock(PartyInfo.class);
    when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

    partyInfoClientEndpoint.onMessage(session, partyInfo);

    verify(partyInfoService).getPartyInfo();
  }
}
