package com.quorum.tessera.serviceloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;

public class ServiceLoaderUtilTest {

  @Test
  public void loadSingle() {

    MyService expected = mock(MyService.class);
    ServiceLoader<MyService> serviceLoader = mock(ServiceLoader.class);
    ServiceLoader.Provider<MyService> provider = mock(ServiceLoader.Provider.class);
    when(provider.get()).thenReturn(expected);
    when(serviceLoader.stream()).thenReturn(Stream.of(provider));

    MyService result = ServiceLoaderUtil.loadSingle(serviceLoader);
    verify(serviceLoader).stream();
    verifyNoMoreInteractions(serviceLoader);

    assertThat(result).isSameAs(expected);
  }

  @Test
  public void ambiguousLookup() {

    final Class type = MyServiceImpl.class;
    ServiceLoader<MyService> serviceLoader = mock(ServiceLoader.class);
    ServiceLoader.Provider<MyService> provider = mock(ServiceLoader.Provider.class);
    when(provider.type()).thenReturn(type);
    ServiceLoader.Provider<MyService> anotherProvider = mock(ServiceLoader.Provider.class);
    when(anotherProvider.type()).thenReturn(type);
    when(serviceLoader.stream()).thenReturn(Stream.of(provider, anotherProvider));

    Throwable result = catchThrowable(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
    assertThat(result)
        .hasMessage(
            "Ambiguous ServiceLoader lookup found multiple instances com.quorum.tessera.serviceloader.MyServiceImpl and com.quorum.tessera.serviceloader.MyServiceImpl.")
        .isExactlyInstanceOf(IllegalStateException.class)
        .isNotNull();

    verify(serviceLoader).stream();
    verifyNoMoreInteractions(serviceLoader);
  }
}
