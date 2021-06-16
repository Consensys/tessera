package com.quorum.tessera.encryption.nacl.kalium;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.Encryptor;
import org.junit.Test;

public class KaliumFactoryTest {

  private final KaliumFactory kaliumFactory = new KaliumFactory();

  @Test
  public void createInstance() {
    final Encryptor result = this.kaliumFactory.create();
    assertThat(kaliumFactory.getType()).isEqualTo("CUSTOM");
    assertThat(result).isNotNull().isExactlyInstanceOf(Kalium.class);
  }
}
