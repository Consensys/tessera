package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Q2TRestAppTest {

  private TransactionManager transactionManager;

  private EncodedPayloadManager encodedPayloadManager;

  private Q2TRestApp q2TRestApp;

  private PrivacyGroupManager privacyGroupManager;

  @Before
  public void beforeTest() throws Exception {
    transactionManager = mock(TransactionManager.class);
    encodedPayloadManager = mock(EncodedPayloadManager.class);
    privacyGroupManager = mock(PrivacyGroupManager.class);

    q2TRestApp = new Q2TRestApp(transactionManager, encodedPayloadManager, privacyGroupManager);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(transactionManager, encodedPayloadManager, privacyGroupManager);
  }

  @Test
  public void getSingletons() {

    Config config = mock(Config.class);
    ConfigFactory configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);
    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      Set<Object> results = q2TRestApp.getSingletons();
      assertThat(results).hasSize(7);
      List<Class> types = results.stream().map(Object::getClass).collect(Collectors.toList());
      assertThat(types)
          .containsExactlyInAnyOrder(
              TransactionResource.class,
              RawTransactionResource.class,
              EncodedPayloadResource.class,
              UpCheckResource.class,
              TransactionResource3.class,
              TransactionResource4.class,
              PrivacyGroupResource.class);
    }
  }

  @Test
  public void getSingletonsOrionClientMode() {

    Config config = mock(Config.class);
    when(config.getClientMode()).thenReturn(ClientMode.ORION);
    ConfigFactory configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);
    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      Set<Object> results = q2TRestApp.getSingletons();
      assertThat(results).hasSize(4);
      List<Class> types = results.stream().map(Object::getClass).collect(Collectors.toList());
      assertThat(types)
          .containsExactlyInAnyOrder(
              BesuTransactionResource.class,
              UpCheckResource.class,
              PrivacyGroupResource.class,
              RawTransactionResource.class);
    }
  }

  @Test
  public void appType() {
    assertThat(q2TRestApp.getAppType()).isEqualTo(AppType.Q2T);
  }

  @Test
  public void getClasses() {
    assertThat(q2TRestApp.getClasses()).contains(Q2TApiResource.class);
  }

  @Test
  public void defaultConstructor() {
    try (var transactionManagerMockedStatic = mockStatic(TransactionManager.class);
        var encodedPayloadManagerMockedStatic = mockStatic(EncodedPayloadManager.class);
        var privacyGroupManagerMockedStatic = mockStatic(PrivacyGroupManager.class);
        var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {

      transactionManagerMockedStatic
          .when(TransactionManager::create)
          .thenReturn(transactionManager);
      encodedPayloadManagerMockedStatic
          .when(EncodedPayloadManager::create)
          .thenReturn(encodedPayloadManager);

      privacyGroupManagerMockedStatic
          .when(PrivacyGroupManager::create)
          .thenReturn(privacyGroupManager);

      new Q2TRestApp();

      transactionManagerMockedStatic.verify(TransactionManager::create);
      transactionManagerMockedStatic.verifyNoMoreInteractions();

      encodedPayloadManagerMockedStatic.verify(EncodedPayloadManager::create);
      encodedPayloadManagerMockedStatic.verifyNoMoreInteractions();

      privacyGroupManagerMockedStatic.verify(PrivacyGroupManager::create);
      privacyGroupManagerMockedStatic.verifyNoMoreInteractions();
    }
  }
}
