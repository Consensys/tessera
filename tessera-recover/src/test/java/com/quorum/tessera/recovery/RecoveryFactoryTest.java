package com.quorum.tessera.recovery;

import com.quorum.tessera.config.*;
import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class RecoveryFactoryTest extends RecoveryTestCase {

    @Test
    public void createRecoveryInstance() {

        final RecoveryFactory recoveryFactory = RecoveryFactory.newFactory();
        final Config config = getConfig();

        Recovery recovery = recoveryFactory.create(config);
        assertThat(recovery).isNotNull();
    }
}
