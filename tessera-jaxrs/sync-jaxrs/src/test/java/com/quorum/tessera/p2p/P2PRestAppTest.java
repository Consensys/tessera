package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.messaging.Inbox;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.ws.rs.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class P2PRestAppTest {

  private RuntimeContext runtimeContext;

  private P2PRestApp p2PRestApp;

  private Enclave enclave;

  private Discovery discovery;

  private PartyStore partyStore;

  private TransactionManager transactionManager;

  private PayloadEncoder payloadEncoder;

  private BatchResendManager batchResendManager;

  private LegacyResendManager legacyResendManager;

  private PrivacyGroupManager privacyGroupManager;

  private Inbox inbox;

  private URI peerUri = URI.create("junit");

  @Before
  public void setUp() throws Exception {

    runtimeContext = mock(RuntimeContext.class);

    enclave = mock(Enclave.class);
    discovery = mock(Discovery.class);
    partyStore = mock(PartyStore.class);
    transactionManager = mock(TransactionManager.class);
    batchResendManager = mock(BatchResendManager.class);
    payloadEncoder = PayloadEncoder.create();
    legacyResendManager = mock(LegacyResendManager.class);
    privacyGroupManager = mock(PrivacyGroupManager.class);
    inbox = mock(Inbox.class);

    p2PRestApp =
        new P2PRestApp(
            discovery,
            enclave,
            partyStore,
            transactionManager,
            batchResendManager,
            payloadEncoder,
            legacyResendManager,
            privacyGroupManager,
            inbox);

    Client client = mock(Client.class);
    when(runtimeContext.getP2pClient()).thenReturn(client);
    when(runtimeContext.isRemoteKeyValidation()).thenReturn(true);

    when(runtimeContext.getPeers()).thenReturn(List.of(peerUri));
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(runtimeContext);
    verifyNoMoreInteractions(enclave);
    verifyNoMoreInteractions(discovery);
    verifyNoMoreInteractions(partyStore);
    verifyNoMoreInteractions(transactionManager);
    verifyNoMoreInteractions(batchResendManager);
    verifyNoMoreInteractions(legacyResendManager);
    verifyNoMoreInteractions(privacyGroupManager);
  }

  @Test
  public void getSingletons() {

    try (var mockedStaticRuntimeContext = mockStatic(RuntimeContext.class); ) {
      mockedStaticRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      Set<Object> results = p2PRestApp.getSingletons();
      assertThat(results).hasSize(6);
      results.forEach(
          o ->
              assertThat(o)
                  .isInstanceOfAny(
                      PrivacyGroupResource.class,
                      PartyInfoResource.class,
                      IPWhitelistFilter.class,
                      TransactionResource.class,
                      UpCheckResource.class,
                      MessageResource.class));

      mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
      mockedStaticRuntimeContext.verifyNoMoreInteractions();
    }

    verify(runtimeContext).isRecoveryMode();
    verify(runtimeContext).getPeers();
    verify(runtimeContext).getP2pClient();
    verify(runtimeContext).isRemoteKeyValidation();
    verify(partyStore).store(peerUri);
  }

  @Test
  public void getSingletonsRecoverP2PApp() {

    when(runtimeContext.isRecoveryMode()).thenReturn(true);

    try (var mockedStaticRuntimeContext = mockStatic(RuntimeContext.class); ) {
      mockedStaticRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      Set<Object> results = p2PRestApp.getSingletons();
      assertThat(results).hasSize(5);
      results.forEach(
          o ->
              assertThat(o)
                  .isInstanceOfAny(
                      PrivacyGroupResource.class,
                      UpCheckResource.class,
                      PartyInfoResource.class,
                      IPWhitelistFilter.class,
                      RecoveryResource.class,
                      MessageResource.class));

      mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
      mockedStaticRuntimeContext.verifyNoMoreInteractions();
    }

    verify(runtimeContext).isRecoveryMode();
    verify(runtimeContext).getPeers();
    verify(runtimeContext).getP2pClient();
    verify(runtimeContext).isRemoteKeyValidation();
    verify(partyStore).store(peerUri);
  }

  @Test
  public void appType() {
    assertThat(p2PRestApp.getAppType()).isEqualTo(AppType.P2P);
  }

  @Test
  public void getClasses() {
    assertThat(p2PRestApp.getClasses()).isNotEmpty();
  }

  @Test
  public void defaultConstructor() {

    try (var enclaveMockedStatic = mockStatic(Enclave.class);
        var discoveryMockedStatic = mockStatic(Discovery.class);
        var partyStoreMockedStatic = mockStatic(PartyStore.class);
        var transactionManagerMockedStatic = mockStatic(TransactionManager.class);
        var payloadEncoderMockedStatic = mockStatic(PayloadEncoder.class);
        var batchResendManagerMockedStatic = mockStatic(BatchResendManager.class);
        var legacyResendManagerMockedStatic = mockStatic(LegacyResendManager.class);
        var privacyGroupManagerMockedStatic = mockStatic(PrivacyGroupManager.class);
        var inboxMockedStatic = mockStatic(Inbox.class)) {

      privacyGroupManagerMockedStatic
          .when(PrivacyGroupManager::create)
          .thenReturn(privacyGroupManager);

      legacyResendManagerMockedStatic
          .when(LegacyResendManager::create)
          .thenReturn(legacyResendManager);
      enclaveMockedStatic.when(Enclave::create).thenReturn(enclave);
      discoveryMockedStatic.when(Discovery::create).thenReturn(discovery);
      partyStoreMockedStatic.when(PartyStore::getInstance).thenReturn(partyStore);
      transactionManagerMockedStatic
          .when(TransactionManager::create)
          .thenReturn(transactionManager);
      payloadEncoderMockedStatic
          .when(PayloadEncoder::create)
          .thenReturn(mock(PayloadEncoder.class));
      batchResendManagerMockedStatic
          .when(BatchResendManager::create)
          .thenReturn(batchResendManager);
      inboxMockedStatic.when(Inbox::create).thenReturn(inbox);

      new P2PRestApp();

      enclaveMockedStatic.verify(Enclave::create);
      enclaveMockedStatic.verifyNoMoreInteractions();

      discoveryMockedStatic.verify(Discovery::create);
      discoveryMockedStatic.verifyNoMoreInteractions();

      partyStoreMockedStatic.verify(PartyStore::getInstance);
      partyStoreMockedStatic.verifyNoMoreInteractions();

      transactionManagerMockedStatic.verify(TransactionManager::create);
      transactionManagerMockedStatic.verifyNoMoreInteractions();

      payloadEncoderMockedStatic.verify(PayloadEncoder::create);
      payloadEncoderMockedStatic.verifyNoMoreInteractions();

      batchResendManagerMockedStatic.verify(BatchResendManager::create);
      batchResendManagerMockedStatic.verifyNoMoreInteractions();

      legacyResendManagerMockedStatic.verify(LegacyResendManager::create);
      legacyResendManagerMockedStatic.verifyNoMoreInteractions();

      privacyGroupManagerMockedStatic.verify(PrivacyGroupManager::create);
      partyStoreMockedStatic.verifyNoMoreInteractions();

      inboxMockedStatic.verify(Inbox::create);
      inboxMockedStatic.verifyNoMoreInteractions();
    }
  }
}
