package com.quorum.tessera.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.service.Service.Status;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceContainerTest {

  private ScheduledExecutorService executorService;

  private ServiceContainer serviceContainer;

  private Service service;

  @Before
  public void onSetup() {
    this.service = mock(Service.class);
    this.executorService = mock(ScheduledExecutorService.class);

    this.serviceContainer = new ServiceContainer(service, executorService, 10L, 10L);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(service, executorService);
  }

  @Test
  public void start() {
    serviceContainer.start();

    verify(executorService).scheduleAtFixedRate(serviceContainer, 10L, 10L, TimeUnit.MILLISECONDS);
  }

  @Test
  public void stop() {
    serviceContainer.stop();

    verify(executorService).shutdown();
    verify(service).stop();
  }

  @Test
  public void runWithServiceWithStoppedStatus() {
    when(service.status()).thenReturn(Service.Status.STOPPED);

    serviceContainer.run();

    verify(service).start();
    verify(service).status();
  }

  @Test
  public void runWithServiceWithNOnStoppedStatus() {

    Stream.of(Status.values())
        .filter(s -> s != Status.STOPPED)
        .forEach(
            s -> {
              when(service.status()).thenReturn(s);
              serviceContainer.run();
            });

    verify(service, times(Status.values().length - 1)).status();
  }

  @Test
  public void constructDefaultInstance() {
    final ServiceContainer sc = new ServiceContainer(service);

    assertThat(sc).isNotNull();
  }

  @Test
  public void serviceErrorsAreIgnored() {
    when(service.status()).thenReturn(Service.Status.STOPPED);
    doThrow(RuntimeException.class).when(service).start();

    serviceContainer.run();

    verify(service).start();
    verify(service).status();
  }
}
