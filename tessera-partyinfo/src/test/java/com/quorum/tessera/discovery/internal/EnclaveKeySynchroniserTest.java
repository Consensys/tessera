package com.quorum.tessera.discovery.internal;

import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.EnclaveKeySynchroniser;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.net.URI;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class EnclaveKeySynchroniserTest {

  private EnclaveKeySynchroniser enclaveKeySynchroniser;

  private Enclave enclave;

  private NetworkStore networkStore;

  private RuntimeContext runtimeContext;

  private MockedStatic<RuntimeContext> mockedStaticRuntimeContext;

  @Before
  public void onSetUp() {

    this.runtimeContext = mock(RuntimeContext.class);
    mockedStaticRuntimeContext = mockStatic(RuntimeContext.class);
    mockedStaticRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

    this.enclave = mock(Enclave.class);
    this.networkStore = mock(NetworkStore.class);
    this.enclaveKeySynchroniser = new EnclaveKeySynchroniserImpl(enclave, networkStore);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(enclave, networkStore, runtimeContext);
    try {
      mockedStaticRuntimeContext.verifyNoMoreInteractions();
    } finally {
      mockedStaticRuntimeContext.close();
    }
  }

  @Test
  public void syncKeysNoChanges() {
    URI uri = URI.create("http://somedomain.com/");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    NodeUri nodeUri = NodeUri.create(uri);

    Set<PublicKey> keys = Set.of(mock(PublicKey.class));
    ActiveNode activeNode = mock(ActiveNode.class);
    when(activeNode.getKeys()).thenReturn(keys);
    when(activeNode.getUri()).thenReturn(nodeUri);

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));
    when(enclave.getPublicKeys()).thenReturn(keys);

    enclaveKeySynchroniser.syncKeys();

    verify(runtimeContext).getP2pServerUri();
    verify(networkStore).getActiveNodes();
    verify(enclave).getPublicKeys();

    mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void syncWithChanges() {

    URI uri = URI.create("http://somedomain.com/");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);

    NodeUri nodeUri = NodeUri.create(uri);

    Set<PublicKey> newKeys = Set.of(mock(PublicKey.class));
    ActiveNode activeNode = mock(ActiveNode.class);
    when(activeNode.getUri()).thenReturn(nodeUri);
    when(activeNode.getKeys()).thenReturn(newKeys);

    when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));
    when(enclave.getPublicKeys()).thenReturn(Set.of(mock(PublicKey.class)));

    enclaveKeySynchroniser.syncKeys();

    verify(runtimeContext).getP2pServerUri();
    verify(networkStore).getActiveNodes();
    verify(enclave).getPublicKeys();
    verify(networkStore).store(any(ActiveNode.class));

    mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
  }

  @Test
  public void syncWithKeysWithoutAnyActiveNodes() {

    final URI uri = URI.create("http://somedomain.com/");
    when(runtimeContext.getP2pServerUri()).thenReturn(uri);
    when(networkStore.getActiveNodes()).thenReturn(Stream.of());

    enclaveKeySynchroniser.syncKeys();

    verify(runtimeContext).getP2pServerUri();
    verify(networkStore).getActiveNodes();
    mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
  }
}
