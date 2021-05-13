package com.quorum.tessera.recovery;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import org.junit.Test;

public class RecoveryFactoryTest extends RecoveryTestCase {

  @Test
  public void createRecoveryInstance() {

    final RecoveryFactory recoveryFactory = RecoveryFactory.newFactory();
    final Config config = getConfig();

    Recovery recovery = recoveryFactory.create(config);
    assertThat(recovery).isNotNull();
  }
}
