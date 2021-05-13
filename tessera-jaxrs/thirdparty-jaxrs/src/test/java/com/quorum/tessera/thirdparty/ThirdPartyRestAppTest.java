package com.quorum.tessera.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThirdPartyRestAppTest {

  private ThirdPartyRestApp thirdParty;

  private Discovery discovery;

  private TransactionManager transactionManager;

  @Before
  public void beforeTest() throws Exception {
    discovery = mock(Discovery.class);
    transactionManager = mock(TransactionManager.class);

    thirdParty = new ThirdPartyRestApp(discovery, transactionManager);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(discovery);
    verifyNoMoreInteractions(transactionManager);
  }

  @Test
  public void getSingletons() {

    Set<Object> results = thirdParty.getSingletons();

    assertThat(results).hasSize(4);
    List<Class> types = results.stream().map(Object::getClass).collect(Collectors.toList());
    assertThat(types)
        .containsExactlyInAnyOrder(
            RawTransactionResource.class,
            PartyInfoResource.class,
            KeyResource.class,
            UpCheckResource.class);
  }

  @Test
  public void getClasses() {
    assertThat(thirdParty.getClasses()).isNotEmpty();
  }

  @Test
  public void appType() {
    assertThat(thirdParty.getAppType()).isEqualTo(AppType.THIRD_PARTY);
  }

  @Test
  public void defaultConstructor() {

    try (var discoveryMockedStatic = mockStatic(Discovery.class);
        var transactionManagerMockedStatic = mockStatic(TransactionManager.class)) {
      discoveryMockedStatic.when(Discovery::create).thenReturn(discovery);
      transactionManagerMockedStatic
          .when(TransactionManager::create)
          .thenReturn(transactionManager);

      ThirdPartyRestApp app = new ThirdPartyRestApp();
      assertThat(app).isNotNull();

      discoveryMockedStatic.verify(Discovery::create);
      transactionManagerMockedStatic.verify(TransactionManager::create);

      discoveryMockedStatic.verifyNoMoreInteractions();
      transactionManagerMockedStatic.verifyNoMoreInteractions();
    }
  }
}
