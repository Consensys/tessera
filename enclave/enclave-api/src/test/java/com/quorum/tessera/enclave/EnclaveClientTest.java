package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.service.Service;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnclaveClientTest {

  private EnclaveClient enclaveClient;

  @Before
  public void onSetUp() {
    this.enclaveClient = mock(EnclaveClient.class); // TODO: not have this as a mock

    doCallRealMethod().when(enclaveClient).validateEnclaveStatus();
  }

  @After
  public void onTearDown() {

    verifyNoMoreInteractions(enclaveClient);
  }

  @Test
  public void enclaveIsUp() {
    when(enclaveClient.status()).thenReturn(Service.Status.STARTED);

    enclaveClient.validateEnclaveStatus();

    verify(enclaveClient).status();

    verify(enclaveClient).validateEnclaveStatus();
  }

  @Test
  public void enclaveIsDown() {
    when(enclaveClient.status()).thenReturn(Service.Status.STOPPED);

    final Throwable throwable = catchThrowable(enclaveClient::validateEnclaveStatus);

    assertThat(throwable).isInstanceOf(EnclaveNotAvailableException.class);

    verify(enclaveClient).status();

    verify(enclaveClient).validateEnclaveStatus();
  }

  @Test
  public void create() {
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
        var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {

      ServiceLoader<EnclaveClient> serviceLoader = mock(ServiceLoader.class);
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(EnclaveClient.class))
          .thenReturn(serviceLoader);

      EnclaveClient.create();

      serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
      serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(EnclaveClient.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
      verifyNoInteractions(serviceLoader);
    }
  }
}
