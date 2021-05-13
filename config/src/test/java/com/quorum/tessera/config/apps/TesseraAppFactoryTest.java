package com.quorum.tessera.config.apps;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import java.util.Optional;
import org.junit.Test;

public class TesseraAppFactoryTest {

  @Test
  public void createExisting() {
    Optional<TesseraApp> result = TesseraAppFactory.create(CommunicationType.REST, AppType.P2P);

    assertThat(result).isPresent();
    assertThat(result.get()).isExactlyInstanceOf(MockTesseraApp.class);
  }

  @Test
  public void createOtherExisting() {
    Optional<TesseraApp> result =
        TesseraAppFactory.create(CommunicationType.WEB_SOCKET, AppType.THIRD_PARTY);

    assertThat(result).isPresent();
    assertThat(result.get()).isExactlyInstanceOf(OtherMockTesseraApp.class);
  }

  @Test
  public void createNonExisting() {
    Optional<TesseraApp> result =
        TesseraAppFactory.create(CommunicationType.WEB_SOCKET, AppType.P2P);

    assertThat(result).isNotPresent();
  }
}
