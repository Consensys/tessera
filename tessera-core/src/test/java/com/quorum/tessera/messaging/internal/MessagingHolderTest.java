package com.quorum.tessera.messaging.internal;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.messaging.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessagingHolderTest{
  Messaging messaging;
  MessagingHolder messagingHolder;
  MessagingImpl messagingImpl;
  private Enclave enclave;
  private Courier courier;
  private Inbox inbox;

  @Before
  public void setUp() {
    enclave = mock(Enclave.class);
    courier = mock(Courier.class);
    inbox = mock(Inbox.class);
    messaging = mock(Messaging.class);
    messagingHolder = mock(MessagingHolder.class);
  }

  @Test
  public void testGetMessaging() {

    assertThat(MessagingHolder.INSTANCE).isNotNull();
    Optional<Messaging> optionalMessaging =  Optional.ofNullable(messaging);
    when(messagingHolder.getMessaging()).thenReturn(optionalMessaging);
    MessagingHolder holder =  MessagingHolder.INSTANCE;
    assertThat(messagingHolder.getMessaging()).isNotNull();
    assertThat(holder.getMessaging()).isNotNull();

    Assert.assertTrue(holder.getMessaging().isPresent());
    assertThat(holder.getMessaging().get()).isNotNull();

  }

  @Test
  public void testStore() {
    messagingImpl = new MessagingImpl(enclave,courier,inbox);
    when(messagingHolder.store(any())).thenReturn(messaging);
    MessagingHolder holder =  MessagingHolder.INSTANCE;
    assertThat(messagingHolder.getMessaging()).isNotNull();
    assertThat(holder.getMessaging()).isNotNull();
    assertThat(messagingImpl).isNotNull();
    Assert.assertNotNull(holder.store(messagingImpl));

  }

}
